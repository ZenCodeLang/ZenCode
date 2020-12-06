/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.util.HashMap;
import java.util.Map;

import org.openzen.zencode.shared.SourceFile;
import org.openzen.zencode.shared.logging.*;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.javashared.prepare.JavaPrepareDefinitionVisitor;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaCompileSpace;

/**
 * @author Hoofdgebruiker
 */
public class JavaSourceCompiler {
	public final JavaSourceFormattingSettings settings;
	public final JavaSourceModule helpers;

	public JavaSourceCompiler(GlobalTypeRegistry registry) {
		helpers = new JavaSourceModule(new Module("helpers"), FunctionParameter.NONE);
		settings = new JavaSourceFormattingSettings.Builder().build();
	}

	public JavaSourceModule compile(IZSLogger logger, SemanticModule module, JavaCompileSpace space, String basePackage) {
		JavaSourceContext context = new JavaSourceContext(logger, helpers, settings, space, module.modulePackage, basePackage);

		JavaSourceModule result = new JavaSourceModule(module.module, module.parameters);
		context.addModule(module.module, result);

		Map<String, JavaSourceFile> sourceFiles = new HashMap<>();
		for (HighLevelDefinition definition : module.definitions.getAll()) {
			String name = getFilename(definition);
			JavaPrepareDefinitionVisitor prepare = new JavaPrepareDefinitionVisitor(context, context.getJavaModule(module.module), name, null);
			JavaClass cls = definition.accept(prepare);

			String filename = cls.getName() + ".java";
			System.out.println("Compiling " + definition.name + " as " + cls.fullName);
			JavaSourceFile sourceFile = sourceFiles.get(filename);
			if (sourceFile == null)
				sourceFiles.put(filename, sourceFile = new JavaSourceFile(JavaSourceCompiler.this, context, cls, module, definition.pkg));

			sourceFile.add(definition);
		}

		for (ScriptBlock scriptBlock : module.scripts) {
			// TODO
		}

		for (JavaSourceFile sourceFile : sourceFiles.values()) {
			sourceFile.prepare(context);
		}

		String baseDirectory = basePackage.replace('.', '/');
		for (Map.Entry<String, JavaSourceFile> entry : sourceFiles.entrySet())
			result.addFile(baseDirectory + "/" + entry.getKey(), entry.getValue().generate());

		context.helperGenerator.write(); // TODO: move this elsewhere
		return result;
	}

	public String getFullName(HighLevelDefinition definition) {
		return definition.pkg.fullName + "." + definition.name;
	}

	private String getFilename(HighLevelDefinition definition) {
		SourceFile source = definition.position.file;
		if (source != null) {
			int slash = Math.max(source.getFilename().lastIndexOf('/'), source.getFilename().lastIndexOf('\\'));
			String filename = source.getFilename().substring(slash < 0 ? 0 : slash + 1);
			filename = filename.substring(0, filename.lastIndexOf('.'));
			return filename;
		} else {
			return definition.name == null ? "Expansion" : definition.name;
		}
	}
}
