/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduledeserializer;

import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.expression.ArrayExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.serialization.*;
import compactio.CompactBytesDataInput;
import compactio.CompactDataInput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zencode.shared.VirtualSourceFile;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.context.ModuleContext;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.compiler.ModuleRegistry;
import org.openzen.zenscript.moduleserialization.DefinitionEncoding;

/**
 * @author Hoofdgebruiker
 */
public class ModuleDeserializer {
	private final ModuleRegistry modules;
	private final AnnotationDefinition[] annotations;
	private final ZSPackage rootPackage;
	private final IZSLogger logger;

	public ModuleDeserializer(
			ModuleRegistry modules,
			AnnotationDefinition[] annotations,
			ZSPackage rootPackage,
			IZSLogger logger) {
		this.modules = modules;
		this.annotations = annotations;
		this.rootPackage = rootPackage;
		this.logger = logger;
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

		AnnotationDefinition[] annotations = new AnnotationDefinition[input.readVarUInt()];
		for (int i = 0; i < annotations.length; i++) {
			String name = stringTable[input.readVarUInt()];
			for (AnnotationDefinition annotation : this.annotations)
				if (annotation.getAnnotationName().equals(name))
					annotations[i] = annotation;

			if (annotations[i] == null)
				throw new DeserializationException("Annotation type not found: " + name);
		}

		CodeReader decoder = new CodeReader(
				input,
				stringTable,
				sourceFiles,
				annotations);

		DeserializingModule[] packagedModules = new DeserializingModule[decoder.readUInt()];
		String[][] dependencyNames = new String[packagedModules.length][];
		for (int i = 0; i < packagedModules.length; i++) {
			int flags = decoder.readUInt();
			String name = decoder.readString();

			ZSPackage modulePackage = rootPackage;
			int packageNameParts = decoder.readUInt();
			for (int j = 0; j < packageNameParts; j++)
				modulePackage = modulePackage.getOrCreatePackage(decoder.readString());

			DeserializingModule[] dependencies = new DeserializingModule[decoder.readUInt()];
			String[] dependencyNames2 = new String[dependencies.length];
			dependencyNames[i] = dependencyNames2;
			for (int j = 0; j < dependencyNames2.length; j++)
				dependencyNames2[j] = decoder.readString();

			packagedModules[i] = new DeserializingModule(
					name,
					dependencies,
					rootPackage,
					modulePackage,
					annotations,
					logger);
			decoder.code.enqueue(new ModuleDecodeScriptsOperation(packagedModules[i]));
			decoder.classes.enqueue(new ModuleDecodeClassesOperation(packagedModules[i], decoder));
		}

		DeserializingModule[] allModules = Arrays.copyOf(packagedModules, packagedModules.length + input.readVarUInt());
		try {
			for (int i = packagedModules.length; i < allModules.length; i++) {
				int flags = input.readVarUInt();
				String name = input.readString();
				allModules[i] = new DeserializingModule(modules.load(name));
				decoder.classes.enqueue(new ModuleDecodeClassesOperation(allModules[i], decoder));
			}
		} catch (CompileException exception) {
			throw new DeserializationException("Caught Compilation Exception: ", exception);
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
			results[i] = module.load();
		}
		return results;
	}

	private HighLevelDefinition deserializeDefinition(CodeReader reader, ModuleContext context, HighLevelDefinition outer) throws DeserializationException {
		int type = reader.readUInt();
		int flags = reader.readUInt();
		CodePosition position = CodePosition.UNKNOWN;
		String name = null;
		TypeParameter[] typeParameters = TypeParameter.NONE;
		Modifiers modifiers = new Modifiers(reader.readUInt());
		if ((flags & DefinitionEncoding.FLAG_POSITION) > 0)
			position = reader.deserializePosition();
		ZSPackage pkg = context.root.getRecursive(reader.readString());
		if ((flags & DefinitionEncoding.FLAG_NAME) > 0)
			name = reader.readString();
		if ((flags & DefinitionEncoding.FLAG_TYPE_PARAMETERS) > 0)
			typeParameters = reader.deserializeTypeParameters(new TypeSerializationContext(null, null, TypeParameter.NONE));

		HighLevelDefinition result;
		switch (type) {
			case DefinitionEncoding.TYPE_CLASS:
				result = new ClassDefinition(position, context.module, pkg, name, modifiers, outer);
				break;
			case DefinitionEncoding.TYPE_STRUCT:
				result = new StructDefinition(position, context.module, pkg, name, modifiers, outer);
				break;
			case DefinitionEncoding.TYPE_INTERFACE:
				result = new InterfaceDefinition(position, context.module, pkg, name, modifiers, outer);
				break;
			case DefinitionEncoding.TYPE_ENUM:
				result = new EnumDefinition(position, context.module, pkg, name, modifiers, outer);
				break;
			case DefinitionEncoding.TYPE_VARIANT:
				result = new VariantDefinition(position, context.module, pkg, name, modifiers, outer);
				break;
			case DefinitionEncoding.TYPE_FUNCTION:
				result = new FunctionDefinition(position, context.module, pkg, name, modifiers, outer);
				break;
			case DefinitionEncoding.TYPE_ALIAS:
				result = new AliasDefinition(position, context.module, pkg, name, modifiers, outer);
				break;
			case DefinitionEncoding.TYPE_EXPANSION:
				result = new ExpansionDefinition(position, context.module, pkg, modifiers);
				break;
			default:
				throw new DeserializationException("Invalid definition type: " + type);
		}

		result.typeParameters = typeParameters;
		decodeMembers(reader, context, result);
		reader.add(result);
		return result;
	}

	private void decodeMembers(
			CodeReader reader,
			ModuleContext moduleContext,
			HighLevelDefinition definition) throws DeserializationException {
		int innerClasses = reader.readUInt();
		for (int i = 0; i < innerClasses; i++) {
			CodePosition position = reader.deserializePosition();
			Modifiers modifiers = new Modifiers(reader.readUInt());
			HighLevelDefinition inner = deserializeDefinition(reader, moduleContext, definition);
			definition.addMember(new InnerDefinitionMember(position, inner, modifiers, definition));
		}

		reader.enqueueMembers(input -> {
			TypeSerializationContext context = new TypeSerializationContext(null, DefinitionTypeID.createThis(definition), definition.typeParameters);
			DefinitionMemberDeserializer memberDeserializer = new DefinitionMemberDeserializer(reader);
			definition.accept(context, memberDeserializer);
		});
	}

	private class ModuleDecodeClassesOperation implements DecodingOperation {
		private final DeserializingModule module;
		private final CodeReader reader;

		public ModuleDecodeClassesOperation(DeserializingModule module, CodeReader reader) {
			this.module = module;
			this.reader = reader;
		}

		@Override
		public void decode(CodeSerializationInput input) throws DeserializationException {
			int numDefinitions = input.readUInt();
			for (int i = 0; i < numDefinitions; i++) {
				HighLevelDefinition definition = deserializeDefinition(reader, module.context, null);
				reader.add(definition);
				module.add(definition);
			}
		}
	}

	private class ModuleDecodeScriptsOperation implements DecodingOperation {
		private final DeserializingModule module;

		public ModuleDecodeScriptsOperation(DeserializingModule module) {
			this.module = module;
		}

		@Override
		public void decode(CodeSerializationInput input) {
			int numberOfScripts = input.readUInt();
			for (int i = 0; i < numberOfScripts; i++) {
				List<Statement> statements = new ArrayList<>();
				//FIXME: Can we deserializePosition here?
				final CodePosition position = CodePosition.UNKNOWN;
				StatementSerializationContext context = new StatementSerializationContext(position, module.context, null);
				int numStatements = input.readUInt();
				for (int j = 0; j < numStatements; j++)
					statements.add(input.deserializeStatement(context));


				//FIXME: Where can we get that header from?
				final ArrayTypeID stringArray = globalTypeRegistry.getArray(BasicTypeID.STRING, 1);
				final FunctionParameter args = new FunctionParameter(stringArray, "args", new ArrayExpression(position, Expression.NONE, stringArray), true);
				final FunctionHeader scriptHeader = new FunctionHeader(BasicTypeID.VOID, args);


				final SourceFile file = position.getFile();
				module.add(new ScriptBlock(file, module.module, rootPackage, scriptHeader, statements));
			}
		}
	}
}
