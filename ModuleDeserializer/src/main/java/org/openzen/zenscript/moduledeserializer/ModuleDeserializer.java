/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduledeserializer;

import compactio.CompactBytesDataInput;
import compactio.CompactDataInput;
import java.util.Arrays;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zencode.shared.VirtualSourceFile;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.context.ModuleContext;
import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationInput;
import org.openzen.zenscript.codemodel.serialization.DecodingOperation;
import org.openzen.zenscript.compiler.CompilationUnit;
import org.openzen.zenscript.compiler.ModuleRegistry;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.moduleserialization.DefinitionEncoding;

/**
 *
 * @author Hoofdgebruiker
 */
public class ModuleDeserializer {
	private final ModuleRegistry modules;
	private final CompilationUnit compilationUnit;
	private final AnnotationDefinition[] annotations;
	private final ZSPackage rootPackage;
	
	public ModuleDeserializer(
			ModuleRegistry modules,
			CompilationUnit compilationUnit,
			AnnotationDefinition[] annotations,
			ZSPackage rootPackage)
	{
		this.modules = modules;
		this.compilationUnit = compilationUnit;
		this.annotations = annotations;
		this.rootPackage = rootPackage;
	}
	
	public SemanticModule[] deserialize(byte[] data) throws DeserializationException {
		CompactDataInput input = new CompactBytesDataInput(data);
		if (input.readInt() != 0x5A43424D)
			throw new DeserializationException("Invalid marker; not a binary module");
		
		int version = input.readVarUInt();
		if (version != 0)
			throw new DeserializationException("Unsupported version: " + version);
		
		String[] stringTable = input.readStringArray();
		SourceFile[] sourceFiles = new SourceFile[input.readVarUInt()];
		for (int i = 0; i < sourceFiles.length; i++)
			sourceFiles[i] = new VirtualSourceFile(input.readString());
		
		CodeReader decoder = new CodeReader(
				input,
				stringTable,
				sourceFiles,
				compilationUnit.globalTypeRegistry);
		
		DeserializingModule[] packagedModules = new DeserializingModule[input.readVarUInt()];
		String[][] dependencyNames = new String[packagedModules.length][];
		for (int i = 0; i < packagedModules.length; i++) {
			int flags = input.readVarUInt();
			String name = input.readString();
			
			ZSPackage modulePackage = rootPackage;
			int packageNameParts = input.readVarUInt();
			for (int j = 0; j < packageNameParts; j++)
				modulePackage = modulePackage.getOrCreatePackage(input.readString());

			DeserializingModule[] dependencies = new DeserializingModule[input.readVarUInt()];
			String[] dependencyNames2 = new String[dependencies.length];
			dependencyNames[i] = dependencyNames2;
			for (int j = 0; j < dependencyNames2.length; j++)
				dependencyNames2[j] = input.readString();
			
			packagedModules[i] = new DeserializingModule(
					name,
					compilationUnit.globalTypeRegistry,
					dependencies,
					rootPackage,
					modulePackage,
					annotations);
			decoder.code.enqueue(new ModuleDecodeScriptsOperation(packagedModules[i]));
			decoder.classes.enqueue(new ModuleDecodeClassesOperation(packagedModules[i], decoder));
		}
		
		DeserializingModule[] allModules = Arrays.copyOf(packagedModules, input.readVarUInt());
		for (int i = packagedModules.length; i < allModules.length; i++) {
			int flags = input.readVarUInt();
			String name = input.readString();
			allModules[i] = new DeserializingModule(modules.load(name));
			decoder.classes.enqueue(new ModuleDecodeClassesOperation(allModules[i], decoder));
		}
		
		System.out.println("Decoding classes");
		decoder.startClasses();
		decoder.classes.decode(decoder);
		System.out.println("Decoding members");
		decoder.startMembers();
		decoder.members.decode(decoder);
		System.out.println("Decoding code");
		decoder.startCode();
		decoder.code.decode(decoder);
		
		SemanticModule[] results = new SemanticModule[packagedModules.length];
		for (int i = 0; i < results.length; i++) {
			DeserializingModule module = packagedModules[i];
			results[i] = module.load(compilationUnit);
		}
		return results;
	}
	
	private class ModuleDecodeClassesOperation implements DecodingOperation {
		private final DeserializingModule module;
		private final CodeReader reader;
		
		public ModuleDecodeClassesOperation(DeserializingModule module, CodeReader reader) {
			this.module = module;
			this.reader = reader;
		}

		@Override
		public void decode(CodeSerializationInput input) {
			int numDefinitions = input.readUInt();
			DefinitionMemberDeserializer memberDeserializer = new DefinitionMemberDeserializer(reader);
			for (int i = 0; i < numDefinitions; i++) {
				HighLevelDefinition definition = deserializeDefinition(reader, module.context);
				reader.add(definition);
				
				TypeContext typeContext = new TypeContext(module.context, definition.typeParameters, module.context.registry.getForMyDefinition(definition));
				reader.members.enqueue(in -> definition.accept(typeContext, memberDeserializer));
			}
		}
		
		private HighLevelDefinition deserializeDefinition(CodeReader reader, ModuleContext context) {
			int type = reader.readUInt();
			int flags = reader.readUInt();
			CodePosition position = CodePosition.UNKNOWN;
			String name = null;
			TypeParameter[] typeParameters = TypeParameter.NONE;
			if ((flags & DefinitionEncoding.FLAG_POSITION) > 0)
				position = reader.deserializePosition();
			ZSPackage pkg = context.root.getRecursive(reader.readString());
			if ((flags & DefinitionEncoding.FLAG_NAME) > 0)
				name = reader.readString();
			if ((flags & DefinitionEncoding.FLAG_TYPE_PARAMETERS) > 0)
				typeParameters = reader.deserializeTypeParameters(new TypeContext(context, TypeParameter.NONE, null));
			
			switch (type) { 
				case DefinitionEncoding.TYPE_CLASS: {
					ClassDefinition result = new ClassDefinition(position, context.module, pkg, name, flags);
					decodeMembers(reader, context, result);
					return result;
				}
				case DefinitionEncoding.TYPE_STRUCT:
				case DefinitionEncoding.TYPE_INTERFACE:
				case DefinitionEncoding.TYPE_ENUM:
				case DefinitionEncoding.TYPE_VARIANT:
				case DefinitionEncoding.TYPE_FUNCTION:
				case DefinitionEncoding.TYPE_ALIAS:
				case DefinitionEncoding.TYPE_EXPANSION:
			}
		}
	}
	
	private void decodeMembers(
			CodeReader reader,
			ModuleContext moduleContext,
			HighLevelDefinition definition)
	{
		reader.enqueueMembers(input -> {
			TypeContext context = new TypeContext(moduleContext, definition.typeParameters, moduleContext.registry.getForMyDefinition(definition));
			definition.addMember(reader.deserializeMember(context));
		});
		
		int innerMembers = reader.readUInt();
		
	}
	
	private class ModuleDecodeScriptsOperation implements DecodingOperation {
		private final DeserializingModule module;
		
		public ModuleDecodeScriptsOperation(DeserializingModule module) {
			this.module = module;
		}

		@Override
		public void decode(CodeSerializationInput input) {
			
		}
	}
}
