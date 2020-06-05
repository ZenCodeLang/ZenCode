/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import java.util.*;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zencode.shared.logging.*;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.javabytecode.compiler.JavaClassWriter;
import org.openzen.zenscript.javabytecode.compiler.JavaScriptFile;
import org.openzen.zenscript.javabytecode.compiler.JavaStatementVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javabytecode.compiler.definitions.JavaDefinitionVisitor;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaCompileSpace;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaParameterInfo;
import org.openzen.zenscript.javashared.prepare.JavaPrepareDefinitionMemberVisitor;
import org.openzen.zenscript.javashared.prepare.JavaPrepareDefinitionVisitor;

/**
 * @author Hoofdgebruiker
 */
public class JavaCompiler {
    
    private int generatedScriptBlockCounter = 0;
	private int expansionCounter = 0;
	private final IZSLogger logger;
    
    public JavaCompiler(IZSLogger logger) {
        this.logger = logger;
    }
	
	public JavaBytecodeModule compile(String packageName, SemanticModule module, JavaCompileSpace space) {
		Map<String, JavaScriptFile> scriptBlocks = new LinkedHashMap<>();
		Set<JavaScriptFile> scriptFilesThatAreActuallyUsedInScripts = new HashSet<>();
		
		JavaBytecodeModule target = new JavaBytecodeModule(module.module, module.parameters, logger);
		JavaBytecodeContext context = new JavaBytecodeContext(target, space, module.modulePackage, packageName, logger);
		context.addModule(module.module, target);
		
		for (HighLevelDefinition definition : module.definitions.getAll()) {
			final String className = getClassName(getFilename(definition));
			String filename = className + "_" + (definition.name == null ? "generated" : definition.name) + "_" + expansionCounter++;
			JavaPrepareDefinitionVisitor definitionPreparer = new JavaPrepareDefinitionVisitor(context, target, filename, null, filename);
			definition.accept(definitionPreparer);
		}
		
		for (HighLevelDefinition definition : module.definitions.getAll()) {
			JavaPrepareDefinitionMemberVisitor memberPreparer = new JavaPrepareDefinitionMemberVisitor(context, target);
			definition.accept(memberPreparer);
		}
		
		for (HighLevelDefinition definition : module.definitions.getAll()) {
            JavaClass cls = definition instanceof ExpansionDefinition ? context.getJavaExpansionClass(definition) : context.getJavaClass(definition);
            JavaScriptFile scriptFile = getScriptFile(scriptBlocks, cls.fullName);
            scriptFile.classWriter.visitSource(definition.position.getFilename(), null);
			target.addClass(cls.internalName, definition.accept(new JavaDefinitionVisitor(context, scriptFile.classWriter)));
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
			if(sourceFile != null) {
                scriptFile.classWriter.visitSource(sourceFile.getFilename(), null);
            }

			String methodName = scriptFile.scriptMethods.isEmpty() ? "run" : "run" + scriptFile.scriptMethods.size();

			// convert scripts into methods (add them to a Scripts class?)
			// (TODO: can we break very long scripts into smaller methods? for the extreme scripts)
			final JavaClassWriter visitor = scriptFile.classWriter;
			JavaMethod method = JavaMethod.getStatic(new JavaClass(context.getPackageName(script.pkg), className, JavaClass.Kind.CLASS), methodName, scriptDescriptor, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
			scriptFile.scriptMethods.add(new JavaScriptMethod(method, module.parameters, javaScriptParameters));

			final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, context.getJavaModule(script.module), new JavaWriter(logger, CodePosition.UNKNOWN, visitor, method, null, null, null));
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
			if(scriptFilesThatAreActuallyUsedInScripts.contains(entry.getValue())) {
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
			// TODO: remove special characters
			return filename.substring(0, filename.lastIndexOf('.')).replace("/", "_");
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
