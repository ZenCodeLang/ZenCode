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
import org.openzen.zenscript.codemodel.type.*;
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
	private final Map<String, Integer> expansionCounters = new HashMap<>();
	private final Map<Class<?>, Integer> generatedCounters = new HashMap<>();

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
			getScriptFile(scriptBlocks, script.pkg.fullName + '/' + className);
		});
		Set<JavaScriptFile> scriptFilesThatAreActuallyUsedInScripts = new HashSet<>();

		JavaBytecodeModule target = new JavaBytecodeModule(module.module, module.parameters, logger);
		target.getEnumMapper().merge(enumMapper);
		JavaBytecodeContext context = new JavaBytecodeContext(target, space, module.modulePackage, packageName, logger);
		JavaCompilingModule compiling = new JavaCompilingModule(context, target);
		context.addModule(module.module, target);

		List<HighLevelDefinition> allDefinitions = new ArrayList<>(module.definitions.getAll());
		allDefinitions.addAll(module.expansions);

		for (HighLevelDefinition definition : allDefinitions) {
			final String className = getClassName(getFilename(definition));
			final String filename;
			if (definition instanceof FunctionDefinition) {
				filename = className;
			} else if (definition instanceof ExpansionDefinition) {
				final String expansionTarget = getFriendlyExpansionTargetName(((ExpansionDefinition) definition).target);
				filename = className + "$expansion$" + expansionTarget + '$' + getCounter(expansionCounters, expansionTarget);
			} else {
				filename = className + "$generated$" + definition.name + '$' + getCounter(generatedCounters, definition.getClass());
			}
			JavaPrepareDefinitionVisitor definitionPreparer = new JavaPrepareDefinitionVisitor(compiling, filename, null, filename);
			definition.accept(definitionPreparer);
		}

		// TODO: topological sort!
		for (HighLevelDefinition definition : allDefinitions) {
			JavaCompilingClass class_ = compiling.getClass(definition);
			JavaPrepareDefinitionMemberVisitor memberPreparer = new JavaPrepareDefinitionMemberVisitor(class_);
			definition.accept(memberPreparer);
		}

		for (HighLevelDefinition definition : allDefinitions) {
			final String internalName;
			final JavaScriptFile scriptFile;
			if (definition instanceof FunctionDefinition) {
				internalName = getClassName(getFilename(definition));
				scriptFile = getScriptFile(scriptBlocks, definition.pkg.fullName.replace('.', '/') + '/' + internalName);
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
			JavaScriptFile scriptFile = getScriptFile(scriptBlocks, script.pkg.fullName + '/' + className);
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
			JavaCompilingMethod compilingMethod = new JavaCompilingMethod(scriptsClass, method, scriptDescriptor);
			scriptFile.scriptMethods.add(new JavaScriptMethod(method, module.parameters, javaScriptParameters));

			final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, context.getJavaModule(script.module), new JavaWriter(logger, CodePosition.UNKNOWN, visitor, compilingMethod, null));
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
			List<String> parts = source.getFilePath();
			return parts.get(parts.size() - 1);
		} else {
			return definition.name == null ? "Expansion" : definition.name;
		}
	}

	private String getClassName(String filename) {
		if (filename == null) {
			class GeneratedBlock {}
			return "generatedBlock" + getCounter(generatedCounters, GeneratedBlock.class);
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
					.replace(File.separatorChar, '/')
					.concat("$");
		}
	}

	private String getFriendlyExpansionTargetName(final TypeID expansionTarget) {
		if (expansionTarget instanceof BasicTypeID) {
			return ((BasicTypeID) expansionTarget).name;
		} else if (expansionTarget instanceof DefinitionTypeID) {
			String defName = ((DefinitionTypeID) expansionTarget).definition.getName();
			int simpleNameBegin = defName.lastIndexOf('.');
			return simpleNameBegin < 0? defName : defName.substring(simpleNameBegin + 1);
		} else if (expansionTarget instanceof AssocTypeID) {
			TypeID key = ((AssocTypeID) expansionTarget).keyType;
			TypeID value = ((AssocTypeID) expansionTarget).valueType;
			return "associative_" + getFriendlyExpansionTargetName(key) + "_2_" + getFriendlyExpansionTargetName(value);
		} else if (expansionTarget instanceof GenericTypeID) {
			return "generic";
		} else if (expansionTarget instanceof ArrayTypeID) {
			return "array_" + getFriendlyExpansionTargetName(((ArrayTypeID) expansionTarget).elementType);
		} else if (expansionTarget instanceof OptionalTypeID) {
			return "optional_" + getFriendlyExpansionTargetName(expansionTarget.withoutOptional());
		}
		return "unknown";
	}

	private JavaScriptFile getScriptFile(Map<String, JavaScriptFile> scriptBlocks, String className) {
		if (!scriptBlocks.containsKey(className)) {
			JavaClassWriter scriptFileWriter = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
			scriptFileWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);
			scriptBlocks.put(className, new JavaScriptFile(scriptFileWriter));
		}

		return scriptBlocks.get(className);
	}

	private <K> int getCounter(Map<K, Integer> counter, K key) {
		return counter.compute(key, (k, v) -> v == null? 0 : ++v);
	}
}
