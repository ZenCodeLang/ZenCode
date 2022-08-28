/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.javabytecode.compiler.JavaClassWriter;
import org.openzen.zenscript.javabytecode.compiler.JavaScriptFile;
import org.openzen.zenscript.javabytecode.compiler.JavaStatementVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javabytecode.compiler.definitions.JavaDefinitionVisitor;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.javashared.compiling.JavaCompilingClass;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;
import org.openzen.zenscript.javashared.compiling.JavaCompilingModule;
import org.openzen.zenscript.javashared.prepare.JavaPrepareDefinitionMemberVisitor;
import org.openzen.zenscript.javashared.prepare.JavaPrepareDefinitionVisitor;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Hoofdgebruiker
 */
public class JavaCompiler {

	private final IZSLogger logger;
	private int generatedScriptBlockCounter = 0;
	private int expansionCounter = 0;

	public JavaCompiler(IZSLogger logger) {
		this.logger = logger;
	}

	public JavaBytecodeModule compile(String packageName, SemanticModule module, JavaCompileSpace space, JavaEnumMapper enumMapper) {
		Map<String, JavaScriptFile> scriptBlocks = new LinkedHashMap<>();
		// Add all the scripts to scriptBlocks before we run through the definitions
		// Scripts with a higher priority load before scripts with a lower priority.
		module.scripts.sort(Comparator.<ScriptBlock>comparingInt(a -> a.file.getOrder()).reversed());
		module.scripts.forEach(script -> {
			final String className = getClassName(script.file == null ? null : script.file.getFilename());
			getScriptFile(scriptBlocks, script.pkg.fullName + "/" + className);
		});
		Set<JavaScriptFile> scriptFilesThatAreActuallyUsedInScripts = new HashSet<>();

		JavaBytecodeModule target = new JavaBytecodeModule(module.module, module.parameters, logger);
		target.getEnumMapper().merge(enumMapper);
		JavaBytecodeContext context = new JavaBytecodeContext(target, space, module.modulePackage, packageName, logger);
		JavaCompilingModule compiling = new JavaCompilingModule(context, target);
		context.addModule(module.module, target);

		for (HighLevelDefinition definition : module.definitions.getAll()) {
			final String className = getClassName(getFilename(definition));
			final String filename;
			if (definition instanceof FunctionDefinition) {
				filename = className;
			} else {
				filename = className + "_" + (definition.name == null ? "generated" : definition.name) + "_" + expansionCounter++;
			}
			JavaPrepareDefinitionVisitor definitionPreparer = new JavaPrepareDefinitionVisitor(compiling, filename, null, filename);
			definition.accept(definitionPreparer);
		}

		// TODO: topological sort!
		for (HighLevelDefinition definition : module.definitions.getAll()) {
			JavaCompilingClass class_ = compiling.getClass(definition);
			JavaPrepareDefinitionMemberVisitor memberPreparer = new JavaPrepareDefinitionMemberVisitor(class_);
			definition.accept(memberPreparer);
		}

		for (HighLevelDefinition definition : module.definitions.getAll()) {
			final String internalName;
			final JavaScriptFile scriptFile;
			if (definition instanceof FunctionDefinition) {
				internalName = getClassName(getFilename(definition));
				scriptFile = getScriptFile(scriptBlocks, module.modulePackage.fullName + "/" + internalName);
				scriptFilesThatAreActuallyUsedInScripts.add(scriptFile);
			} else {
				JavaClass cls = definition instanceof ExpansionDefinition ? context.getJavaExpansionClass(definition) : context
						.getJavaClass(definition);
				scriptFile = getScriptFile(scriptBlocks, cls.fullName);
				internalName = cls.internalName;
			}
			scriptFile.classWriter.visitSource(definition.position.getFilename(), null);
			target.addClass(internalName, definition.accept(new JavaDefinitionVisitor(context, compiling, scriptFile.classWriter)));
		}

		FunctionHeader scriptHeader = new FunctionHeader(BasicTypeID.VOID, module.parameters);
		String scriptDescriptor = context.getMethodDescriptor(scriptHeader);
		JavaParameterInfo[] javaScriptParameters = new JavaParameterInfo[module.parameters.length];
		for (int i = 0; i < module.parameters.length; i++) {
			FunctionParameter parameter = module.parameters[i];
			JavaParameterInfo javaParameter = new JavaParameterInfo(i, context.getDescriptor(parameter.type));
			target.setParameterInfo(parameter, javaParameter);
			javaScriptParameters[i] = javaParameter;
		}

		for (ScriptBlock script : module.scripts) {
			final SourceFile sourceFile = script.file;
			final String className = getClassName(sourceFile == null ? null : sourceFile.getFilename());
			JavaScriptFile scriptFile = getScriptFile(scriptBlocks, script.pkg.fullName + "/" + className);
			scriptFilesThatAreActuallyUsedInScripts.add(scriptFile);
			if (sourceFile != null) {
				scriptFile.classWriter.visitSource(sourceFile.getFilename(), null);
			}

			String methodName = scriptFile.scriptMethods.isEmpty() ? "run" : "run" + scriptFile.scriptMethods.size();

			// convert scripts into methods (add them to a Scripts class?)
			// (TODO: can we break very long scripts into smaller methods? for the extreme scripts)
			final JavaClassWriter visitor = scriptFile.classWriter;
			JavaClass scriptsClass = new JavaClass(context.getPackageName(script.pkg), className, JavaClass.Kind.CLASS);
			JavaNativeMethod method = JavaNativeMethod.getStatic(scriptsClass, methodName, scriptDescriptor, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
			JavaCompilingMethod compilingMethod = new JavaCompilingMethod(scriptsClass, method);
			scriptFile.scriptMethods.add(new JavaScriptMethod(method, module.parameters, javaScriptParameters));

			final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, context.getJavaModule(script.module), new JavaWriter(logger, CodePosition.UNKNOWN, visitor, compilingMethod, null, null, null));
			statementVisitor.start();
			for (Statement statement : script.statements) {
				statement.accept(statementVisitor);
			}
			statementVisitor.end();
		}

		for (Map.Entry<String, JavaScriptFile> entry : scriptBlocks.entrySet()) {
			for (JavaScriptMethod method : entry.getValue().scriptMethods)
				target.addScript(method);

			entry.getValue().classWriter.visitEnd();
			if (scriptFilesThatAreActuallyUsedInScripts.contains(entry.getValue())) {
				target.addClass(entry.getKey(), entry.getValue().classWriter.toByteArray());
			}
		}

		return target;
	}

	private String getFilename(HighLevelDefinition definition) {
		SourceFile source = definition.position.file;
		if (source != null) {
			//int slash = Math.max(source.getFilename().lastIndexOf('/'), source.getFilename().lastIndexOf('\\'));
			//String filename = source.getFilename().substring(slash < 0 ? 0 : slash + 1);
			//filename = filename.substring(0, filename.lastIndexOf('.'));
			//return filename;
			return source.getFilename();
		} else {
			return definition.name == null ? "Expansion" : definition.name;
		}
	}

	private String getClassName(String filename) {
		if (filename == null) {
			return "generatedBlock" + (generatedScriptBlockCounter++);
		} else {
			// TODO: find all special characters
			final String specialCharRegex = Stream.of('/', '\\', '.', ';')
					.filter(character -> character != File.separatorChar)
					.map(String::valueOf)
					.collect(Collectors.joining("", "[", "]"));

			return filename
					.substring(0, filename.lastIndexOf('.')) //remove the .zs part
					.replaceAll(specialCharRegex, "_")
					.replace('[', '_')
					.replace(File.separatorChar, '/');
		}
	}

	private JavaScriptFile getScriptFile(Map<String, JavaScriptFile> scriptBlocks, String className) {
		if (!scriptBlocks.containsKey(className)) {
			JavaClassWriter scriptFileWriter = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
			scriptFileWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);
			scriptBlocks.put(className, new JavaScriptFile(scriptFileWriter));
		}

		return scriptBlocks.get(className);
	}
}
