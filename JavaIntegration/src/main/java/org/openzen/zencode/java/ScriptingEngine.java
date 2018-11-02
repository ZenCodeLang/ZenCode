/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.java;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.ModuleSpace;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.type.storage.StorageType;
import org.openzen.zenscript.javabytecode.JavaBytecodeRunUnit;
import org.openzen.zenscript.javabytecode.JavaCompiler;
import org.openzen.zenscript.javashared.SimpleJavaCompileSpace;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.BracketExpressionParser;
import org.openzen.zenscript.parser.ParsedFile;
import org.openzen.zenscript.parser.ZippedPackage;
import org.openzen.zenscript.validator.Validator;

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
	
	public ScriptingEngine() {
		space = new ModuleSpace(registry, new ArrayList<>(), StorageType.getStandard());
		
		try {
			ZippedPackage stdlibs = new ZippedPackage(ScriptingEngine.class.getResourceAsStream("/StdLibs.zip"));
			SemanticModule stdlibModule = stdlibs.loadModule(space, "stdlib", null, new SemanticModule[0], stdlib);
			stdlibModule = Validator.validate(stdlibModule, error -> System.out.println(error.toString()));
			space.addModule("stdlib", stdlibModule);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (CompileException ex) {
			throw new RuntimeException(ex);
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public JavaNativeModule createNativeModule(String name, String basePackage, JavaNativeModule... dependencies) {
		ZSPackage testPackage = new ZSPackage(space.rootPackage, name);
		return new JavaNativeModule(testPackage, name, basePackage, registry, false, dependencies);
	}
	
	public void registerNativeProvided(JavaNativeModule module) throws CompileException {
		SemanticModule semantic = Validator.validate(
				module.toSemantic(space),
				entry -> System.out.println(entry));
		if (!semantic.isValid())
			return;
		
		space.addModule(module.module.name, semantic);
		nativeModules.add(module);
		
		for (Map.Entry<String, ISymbol> globalEntry : module.globals.entrySet())
			space.addGlobal(globalEntry.getKey(), globalEntry.getValue());
	}
	
	public SemanticModule createScriptedModule(String name, SourceFile[] sources, String... dependencies) throws ParseException {
		return createScriptedModule(name, sources, null, dependencies);
	}
	
	public SemanticModule createScriptedModule(String name, SourceFile[] sources, BracketExpressionParser bracketParser, String... dependencies) throws ParseException {
		Module scriptModule = new Module(name);
		CompilingPackage scriptPackage = new CompilingPackage(new ZSPackage(space.rootPackage, name), scriptModule);
		
		ParsedFile[] files = new ParsedFile[sources.length];
		for (int i = 0; i < sources.length; i++)
			files[i] = ParsedFile.parse(scriptPackage, bracketParser, sources[i]);
		
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
				ex -> ex.printStackTrace());
		if (!scripts.isValid())
			return scripts;
		
		return Validator.validate(
				scripts.normalize(),
				error -> System.out.println(error.toString()));
	}
	
	public void registerCompiled(SemanticModule module) {
		compiledModules.add(module);
	}
	
	public void run() {
		SimpleJavaCompileSpace javaSpace = new SimpleJavaCompileSpace(registry);
		for (JavaNativeModule nativeModule : nativeModules)
			javaSpace.register(nativeModule.getCompiled());
		
		JavaCompiler compiler = new JavaCompiler();
		
		JavaBytecodeRunUnit runUnit = new JavaBytecodeRunUnit();
		for (SemanticModule compiled : compiledModules)
			runUnit.add(compiler.compile(compiled.name, compiled, javaSpace));
		if (debug)
			runUnit.dump(new File("classes"));
		runUnit.run();
	}
}
