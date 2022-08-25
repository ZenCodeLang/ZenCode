/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.java;

import org.openzen.zencode.java.logger.ScriptingEngineLogger;
import org.openzen.zencode.java.logger.ScriptingEngineStreamLogger;
import org.openzen.zencode.java.module.JavaNativeModule;
import org.openzen.zencode.java.module.converters.JavaNativeConverterBuilder;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.ModuleSpace;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.globals.IGlobal;
import org.openzen.zenscript.javabytecode.JavaBytecodeRunUnit;
import org.openzen.zenscript.javabytecode.JavaCompiler;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaEnumMapper;
import org.openzen.zenscript.javashared.SimpleJavaCompileSpace;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.BracketExpressionParser;
import org.openzen.zenscript.parser.ParsedFile;
import org.openzen.zenscript.parser.ZippedPackage;
import org.openzen.zenscript.validator.Validator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Hoofdgebruiker
 */
public class ScriptingEngine {
	public final ScriptingEngineLogger logger;
	public final ZSPackage root = ZSPackage.createRoot();
	private final ZSPackage stdlib = root.getOrCreatePackage("stdlib");
	private final ModuleSpace space;
	private final List<JavaNativeModule> nativeModules = new ArrayList<>();
	private final List<SemanticModule> compiledModules = new ArrayList<>();
	public boolean debug = false;

	public ScriptingEngine() {
        this(new ScriptingEngineStreamLogger());
    }

	public ScriptingEngine(ScriptingEngineLogger logger) {
		this(logger, ScriptingEngine.class::getResourceAsStream);
	}
    
    public ScriptingEngine(ScriptingEngineLogger logger, Function<String, InputStream> resourceGetter) {
        this.space = new ModuleSpace(new ArrayList<>());
        this.logger = logger;
        try {
            ZippedPackage stdlibs = new ZippedPackage(resourceGetter.apply("/StdLibs.jar"));
			registerLibFromStdLibs(logger, stdlibs, "stdlib", stdlib);
			registerLibFromStdLibs(logger, stdlibs, "math", root.getOrCreatePackage("math"));
		} catch (CompileException | ParseException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }


	private void registerLibFromStdLibs(ScriptingEngineLogger logger, ZippedPackage stdlibs, String name, ZSPackage zsPackage) throws ParseException, CompileException {
		SemanticModule stdlibModule = stdlibs.loadModule(space, name, null, SemanticModule.NONE, FunctionParameter.NONE, zsPackage, logger);
		stdlibModule = Validator.validate(stdlibModule, logger);
		space.addModule(name, stdlibModule);
		registerCompiled(stdlibModule);
	}

	public JavaNativeModule createNativeModule(String name, String basePackage, JavaNativeModule... dependencies) {
		ZSPackage testPackage = new ZSPackage(space.rootPackage, name);
		return new JavaNativeModule(space, logger, testPackage, name, basePackage, dependencies, space.rootPackage);
	}

	public JavaNativeModule createNativeModule(String name, String basePackage, JavaNativeModule[] dependencies, JavaNativeConverterBuilder nativeConverterBuilder) {
		ZSPackage testPackage = new ZSPackage(space.rootPackage, name);
		return new JavaNativeModule(space, logger, testPackage, name, basePackage, dependencies, nativeConverterBuilder, space.rootPackage);
	}

	public void registerNativeProvided(JavaNativeModule module) throws CompileException {
		SemanticModule semantic = Validator.validate(module.toSemantic(), logger);
		if (!semantic.isValid())
			return;

		space.addModule(module.getModule().name, semantic);
		nativeModules.add(module);

		for (Map.Entry<String, IGlobal> globalEntry : module.getGlobals().entrySet())
			space.addGlobal(globalEntry.getKey(), globalEntry.getValue());
	}

	public SemanticModule createScriptedModule(String name, SourceFile[] sources, String... dependencies) throws ParseException {
		return createScriptedModule(name, sources, null, FunctionParameter.NONE, dependencies);
	}

	public SemanticModule createScriptedModule(
			String name,
			SourceFile[] sources,
			BracketExpressionParser bracketParser,
			FunctionParameter[] scriptParameters,
			String... dependencies) throws ParseException {
		Module scriptModule = new Module(name);
		CompilingPackage scriptPackage = new CompilingPackage(new ZSPackage(space.rootPackage, name), scriptModule);

		ParsedFile[] files = new ParsedFile[sources.length];
		for (int i = 0; i < sources.length; i++) {
			logger.logSourceFile(sources[i]);
			files[i] = ParsedFile.parse(bracketParser, sources[i]);
		}

		SemanticModule[] dependencyModules = new SemanticModule[dependencies.length + 1];
		dependencyModules[0] = space.getModule("stdlib");
		for (int i = 0; i < dependencies.length; i++) {
			dependencyModules[i + 1] = space.getModule(dependencies[i]);
		}

		SemanticModule scripts = ParsedFile.compileSyntaxToSemantic(
				dependencyModules,
				scriptPackage,
				files,
				space,
				scriptParameters,
				logger);
		if (!scripts.isValid())
			return scripts;

		return Validator.validate(
				scripts.normalize(),
				logger);
	}

	public void registerCompiled(SemanticModule module) {
		compiledModules.add(module);
	}

	public void run() {
		run(Collections.emptyMap());
	}

	public void run(Map<FunctionParameter, Object> arguments) {
		run(arguments, this.getClass().getClassLoader());
	}

	public JavaBytecodeRunUnit createRunUnit() {
		SimpleJavaCompileSpace javaSpace = new SimpleJavaCompileSpace();
		JavaEnumMapper enumMapper = new JavaEnumMapper();
		for (JavaNativeModule nativeModule : nativeModules) {
			JavaCompiledModule compiled = nativeModule.getCompiled();
			javaSpace.register(compiled);
			enumMapper.merge(compiled.getEnumMapper());
		}

		JavaCompiler compiler = new JavaCompiler(logger);
		JavaBytecodeRunUnit runUnit = new JavaBytecodeRunUnit(logger);
		for (SemanticModule compiled : compiledModules)
			runUnit.add(compiler.compile(compiled.name, compiled, javaSpace, enumMapper));
		if (debug)
			runUnit.dump(new File("classes"));

		return runUnit;
	}

	public void run(Map<FunctionParameter, Object> arguments, ClassLoader parentClassLoader) {
		JavaBytecodeRunUnit runUnit = createRunUnit();

		try {
			runUnit.run(arguments, parentClassLoader);
		} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			logger.throwingErr(e.getCause().getMessage(), e.getCause());
		}
	}

	public List<JavaNativeModule> getNativeModules() {
		return Collections.unmodifiableList(this.nativeModules);
	}
}
