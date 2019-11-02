/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.java;

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
import java.util.function.*;

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
	
	public IZSLogger logger;
	
	public ScriptingEngine() {
		space = new ModuleSpace(registry, new ArrayList<>(), StorageType.getStandard());
		
		try {
			File stdlibsFolder = new File(ScriptingEngine.class.getResource("/StdLibs").getPath());
            FolderPackage stdlibs = new FolderPackage(stdlibsFolder);
            SemanticModule stdlibModule = stdlibs.loadModule(space, "stdlib", null, new SemanticModule[0], FunctionParameter.NONE, stdlib);
			stdlibModule = Validator.validate(stdlibModule, error -> System.out.println(error.toString()));
			space.addModule("stdlib", stdlibModule);
		} catch (CompileException | ParseException ex) {
			throw new RuntimeException(ex);
		}
		this.logger = new EmptyLogger();
    }
    
    public ScriptingEngine(IZSLogger logger) {
        this();
        this.logger = logger;
    }
	
	public JavaNativeModule createNativeModule(String name, String basePackage, JavaNativeModule... dependencies) {
		ZSPackage testPackage = new ZSPackage(space.rootPackage, name);
		return new JavaNativeModule(testPackage, name, basePackage, registry, dependencies);
	}
	
	public void registerNativeProvided(JavaNativeModule module) throws CompileException {
		SemanticModule semantic = Validator.validate(
				module.toSemantic(space), System.out::println);
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
		return createScriptedModule(name, sources, bracketParser, scriptParameters, Throwable::printStackTrace, System.out::println, sourceFile -> System.out.println("Loading " + sourceFile.getFilename()), dependencies);
	}
	
	public SemanticModule createScriptedModule(
			String name,
			SourceFile[] sources,
			BracketExpressionParser bracketParser,
			FunctionParameter[] scriptParameters,
			Consumer<CompileException> compileExceptionConsumer,
			Consumer<ValidationLogEntry> validatorErrorConsumer,
			Consumer<SourceFile> sourceFileConsumer,
			String... dependencies) throws ParseException
	{
		Module scriptModule = new Module(name);
		CompilingPackage scriptPackage = new CompilingPackage(new ZSPackage(space.rootPackage, name), scriptModule);
		
		ParsedFile[] files = new ParsedFile[sources.length];
		for (int i = 0; i < sources.length; i++) {
			sourceFileConsumer.accept(sources[i]);
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
				compileExceptionConsumer);
		if (!scripts.isValid())
			return scripts;
		
		return Validator.validate(
				scripts.normalize(),
				validatorErrorConsumer);
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

	public void run(Map<FunctionParameter, Object> arguments, ClassLoader parentClassLoader) {
		SimpleJavaCompileSpace javaSpace = new SimpleJavaCompileSpace(registry);
		for (JavaNativeModule nativeModule : nativeModules)
			javaSpace.register(nativeModule.getCompiled());

		JavaCompiler compiler = new JavaCompiler();

		JavaBytecodeRunUnit runUnit = new JavaBytecodeRunUnit();
		for (SemanticModule compiled : compiledModules)
			runUnit.add(compiler.compile(compiled.name, compiled, javaSpace));
		if (debug)
			runUnit.dump(new File("classes"));
        
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
