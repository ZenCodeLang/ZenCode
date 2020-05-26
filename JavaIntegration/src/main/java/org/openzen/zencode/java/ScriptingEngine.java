/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.java;

import org.openzen.zencode.java.logger.*;
import org.openzen.zencode.shared.*;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.context.*;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.storage.*;
import org.openzen.zenscript.javabytecode.*;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.lexer.*;
import org.openzen.zenscript.parser.*;
import org.openzen.zenscript.validator.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 *
 * @author Hoofdgebruiker
 */
public class ScriptingEngine {
	private final ZSPackage root = ZSPackage.createRoot();
	private final ZSPackage stdlib = new ZSPackage(root, "stdlib");
	public final GlobalTypeRegistry registry = new GlobalTypeRegistry(stdlib);
	private final ModuleSpace space;
	
	private final List<JavaNativeModule> nativeModules = new ArrayList<>();
	private final List<SemanticModule> compiledModules = new ArrayList<>();
	
	public boolean debug = false;
	
	public final ScriptingEngineLogger logger;
	
	public ScriptingEngine() {
        this(new ScriptingEngineStreamLogger());
    }
    
    public ScriptingEngine(ScriptingEngineLogger logger) {
        this.space = new ModuleSpace(registry, new ArrayList<>(), StorageType.getStandard());
        this.logger = logger;
        try {
            ZippedPackage stdlibs = new ZippedPackage(ScriptingEngine.class.getResourceAsStream("/StdLibs.jar"));
            SemanticModule stdlibModule = stdlibs.loadModule(space, "stdlib", null, SemanticModule.NONE, FunctionParameter.NONE, stdlib, logger);
            stdlibModule = Validator.validate(stdlibModule, logger);
            space.addModule("stdlib", stdlibModule);
            registerCompiled(stdlibModule);
        } catch (CompileException | ParseException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
	
	public JavaNativeModule createNativeModule(String name, String basePackage, JavaNativeModule... dependencies) {
		ZSPackage testPackage = new ZSPackage(space.rootPackage, name);
		return new JavaNativeModule(logger, testPackage, name, basePackage, registry, dependencies);
	}
	
	public void registerNativeProvided(JavaNativeModule module) throws CompileException {
		SemanticModule semantic = Validator.validate(
				module.toSemantic(space), logger);
		if (!semantic.isValid())
			return;
		
		space.addModule(module.module.name, semantic);
		nativeModules.add(module);
		
		for (Map.Entry<String, ISymbol> globalEntry : module.globals.entrySet())
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
			String... dependencies) throws ParseException
	{
		Module scriptModule = new Module(name);
		CompilingPackage scriptPackage = new CompilingPackage(new ZSPackage(space.rootPackage, name), scriptModule);
		
		ParsedFile[] files = new ParsedFile[sources.length];
		for (int i = 0; i < sources.length; i++) {
			logger.logSourceFile(sources[i]);
			files[i] = ParsedFile.parse(scriptPackage, bracketParser, sources[i]);
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
        SimpleJavaCompileSpace javaSpace = new SimpleJavaCompileSpace(registry);
        for (JavaNativeModule nativeModule : nativeModules)
            javaSpace.register(nativeModule.getCompiled());
        
        JavaCompiler compiler = new JavaCompiler(logger);
        
        JavaBytecodeRunUnit runUnit = new JavaBytecodeRunUnit(logger);
        for (SemanticModule compiled : compiledModules)
            runUnit.add(compiler.compile(compiled.name, compiled, javaSpace));
        if (debug)
            runUnit.dump(new File("classes"));
        
        return runUnit;
    }

	public void run(Map<FunctionParameter, Object> arguments, ClassLoader parentClassLoader) {
	    JavaBytecodeRunUnit runUnit = createRunUnit();
        
        try {
            runUnit.run(arguments, parentClassLoader);
        } catch(ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            logger.throwingErr(e.getCause().getMessage(), e.getCause());
        }
    }
	
	public List<JavaNativeModule> getNativeModules() {
		return Collections.unmodifiableList(this.nativeModules);
	}
}
