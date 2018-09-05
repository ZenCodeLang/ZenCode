/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer;

import org.openzen.zenscript.codemodel.serialization.EncodingOperation;
import compactio.CompactBytesDataOutput;
import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.moduleserialization.ModuleEncoding;
import org.openzen.zenscript.moduleserializer.encoder.DefinitionSerializer;
import org.openzen.zenscript.codemodel.context.StatementContext;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;

/**
 *
 * @author Hoofdgebruiker
 */
public class ModuleSerializer {
	private final SerializationOptions options;
	
	public ModuleSerializer(SerializationOptions options) {
		this.options = options;
	}
	
	public byte[] serialize(List<SemanticModule> modules) {
		TableBuilder tableBuilder = new TableBuilder(options);
		
		List<EncodingModule> encodingModules = new ArrayList<>();
		for (SemanticModule module : modules) {
			encodingModules.add(tableBuilder.register(module.module));
			
			tableBuilder.writeString(module.name);
			for (String part : getPackageName(module.modulePackage))
				tableBuilder.writeString(part);
		}
		
		for (SemanticModule module : modules) {
			EncodingModule encodedModule = tableBuilder.register(module.module);
			for (HighLevelDefinition definition : module.definitions.getAll()) {
				encodedModule.add(EncodingDefinition.complete(definition));
				tableBuilder.serialize(definition);
			}
			
			for (ScriptBlock script : module.scripts) {
				StatementContext context = new StatementContext();
				for (Statement statement : script.statements) {
					tableBuilder.serialize(context, statement);
				}
			}
		}
		
		SourceFile[] sourceFiles = tableBuilder.getSourceFileList();
		String[] strings = tableBuilder.getStrings();
		
		CompactBytesDataOutput output = new CompactBytesDataOutput();
		ModuleEncoder encoder = new ModuleEncoder(
				output,
				options,
				strings,
				sourceFiles);
		
		output.writeInt(0x5A43424D); // 'ZCBM' = ZenCode Binary Module
		output.writeVarUInt(0); // version
		output.writeStringArray(strings);
		output.writeVarUInt(sourceFiles.length);
		for (SourceFile file : sourceFiles)
			output.writeString(file.getFilename());
		
		output.writeVarUInt(encodingModules.size());
		System.out.println("Encoding list of modules");
		for (int i = 0; i < encodingModules.size(); i++) {
			EncodingModule encodingModule = encodingModules.get(i);
			SemanticModule module = modules.get(i);
			
			int flags = ModuleEncoding.FLAG_CODE;
			output.writeVarUInt(flags);
			output.writeString(encodingModule.getName());
			String[] packageName = getPackageName(module.modulePackage);
			output.writeStringArray(packageName);
			
			output.writeVarUInt(module.dependencies.length);
			for (SemanticModule dependency : module.dependencies)
				output.writeString(dependency.name);
			
			encoder.code.enqueue(new ModuleEncodeScriptsOperation(module.scripts));
			encoder.classes.enqueue(new ModuleEncodeClassesOperation(options, encodingModule, encoder));
		}
		
		output.writeVarUInt(tableBuilder.modules.size() - encodingModules.size()); // number of modules defined (first is always the current module)
		for (int i = encodingModules.size(); i < tableBuilder.modules.size(); i++) {
			EncodingModule encodingModule = tableBuilder.modules.get(i);
			System.out.println("Encoding dependency module " + encodingModule.getName());
			
			output.writeVarUInt(0);
			output.writeString(encodingModule.getName());
			encoder.classes.enqueue(new ModuleEncodeClassesOperation(options, encodingModule, encoder));
		}
		
		System.out.println("Encoding classes");
		encoder.startClasses();
		encoder.classes.encode(encoder);
		System.out.println("Encoding members");
		encoder.startMembers();
		encoder.members.encode(encoder);
		System.out.println("Encoding code");
		encoder.startCode();
		encoder.code.encode(encoder);
		return output.asByteArray();
	}
	
	private static String[] getPackageName(ZSPackage pkg) {
		List<String> nameReverse = new ArrayList<>();
		while (pkg.parent != null) {
			nameReverse.add(pkg.name);
			pkg = pkg.parent;
		}
		
		String[] result = new String[nameReverse.size()];
		for (int i = 0; i < nameReverse.size(); i++)
			result[nameReverse.size() - i - 1] = nameReverse.get(i);
		return result;
	}
	
	private static class ModuleEncodeClassesOperation implements EncodingOperation {
		private final SerializationOptions options;
		private final EncodingModule module;
		private final ModuleEncoder encoder;
		
		public ModuleEncodeClassesOperation(
				SerializationOptions options,
				EncodingModule module,
				ModuleEncoder encoder)
		{
			this.options = options;
			this.module = module;
			this.encoder = encoder;
		}

		@Override
		public void encode(CodeSerializationOutput output) {
			output.writeUInt(module.definitions.size());
			
			DefinitionSerializer definitionEncoder = new DefinitionSerializer(options, output);
			for (EncodingDefinition definition : module.definitions) {
				System.out.println("Encoding definition " + definition.definition.name);
				definition.definition.accept(definitionEncoder);
				encoder.add(definition.definition);
			}
		}
	}
	
	private static class ModuleEncodeScriptsOperation implements EncodingOperation {
		private final List<ScriptBlock> scripts;
		
		public ModuleEncodeScriptsOperation(List<ScriptBlock> scripts) {
			this.scripts = scripts;
		}

		@Override
		public void encode(CodeSerializationOutput output) {
			output.writeUInt(scripts.size());
			for (ScriptBlock script : scripts) {
				output.writeUInt(script.statements.size());
				StatementContext context = new StatementContext();
				for (Statement statement : script.statements) {
					output.serialize(context, statement);
				}
			}
		}
	}
}
