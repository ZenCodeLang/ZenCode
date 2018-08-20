/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.ModuleTypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.constructor.module.ModuleSpace;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.parser.BracketExpressionParser;
import org.openzen.zenscript.parser.ParsedFile;
import org.openzen.zenscript.parser.PrecompilationState;

/**
 *
 * @author Hoofdgebruiker
 */
public class Module {
	public final String name;
	public final String[] dependencies;
	public final File sourceDirectory;
	public final String packageName;
	public final String javaPackageName;
	public final String host;
	private final Consumer<CompileException> exceptionLogger;
	
	public Module(String name, File directory, File moduleFile, Consumer<CompileException> exceptionLogger) throws IOException {
		this.name = name;
		this.sourceDirectory = new File(directory, "src");
		this.exceptionLogger = exceptionLogger;
		
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(moduleFile));
		JSONObject json = new JSONObject(new JSONTokener(input));
		packageName = json.getString("package");
		javaPackageName = json.optString("javaPackageName", packageName);
		host = json.getString("host");
		JSONArray dependencies = json.optJSONArray("dependencies");
		if (dependencies == null) {
			this.dependencies = new String[0];
		} else {
			this.dependencies = new String[dependencies.length()];
			for (int i = 0; i < dependencies.length(); i++)
				this.dependencies[i] = dependencies.getString(i);
		}
	}
	
	public ParsedFile[] parse(CompilingPackage compilingPackage) throws IOException {
		// TODO: load bracket parsers from host plugins
		List<ParsedFile> files = new ArrayList<>();
		parse(files, compilingPackage, null, sourceDirectory);
		return files.toArray(new ParsedFile[files.size()]);
	}
	
	private void parse(List<ParsedFile> files, CompilingPackage pkg, BracketExpressionParser bracketParser, File directory) throws IOException {
		for (File file : directory.listFiles()) {
			if (file.getName().endsWith(".zs")) {
				try {
					files.add(ParsedFile.parse(pkg, bracketParser, file));
				} catch (CompileException ex) {
					exceptionLogger.accept(ex);
				}
			} else if (file.isDirectory()) {
				CompilingPackage innerPackage = pkg.getOrCreatePackage(file.getName());
				pkg.addPackage(file.getName(), innerPackage);
				parse(files, innerPackage, bracketParser, file);
			}
		}
	}
	
	public static SemanticModule compileSyntaxToSemantic(
			String name,
			String[] dependencies,
			CompilingPackage pkg,
			ParsedFile[] files,
			ModuleSpace registry,
			Consumer<CompileException> exceptionLogger) {
		// We are considering all these files to be in the same package, so make
		// a single PackageDefinition instance. If these files were in multiple
		// packages, we'd need an instance for every package.
		PackageDefinitions definitions = new PackageDefinitions();
		for (ParsedFile file : files) {
			// listDefinitions will merely register all definitions (classes,
			// interfaces, functions ...) so they can later be available to
			// the other files as well. It doesn't yet compile anything.
			file.listDefinitions(definitions);
		}
		
		ZSPackage rootPackage = registry.collectPackages();
		List<ExpansionDefinition> expansions = registry.collectExpansions();
		definitions.registerExpansionsTo(expansions);
		
		Map<String, ISymbol> globals = registry.collectGlobals();
		boolean failed = false;
		
		ModuleTypeResolutionContext moduleContext = new ModuleTypeResolutionContext(
				registry.compilationUnit.globalTypeRegistry,
				registry.getAnnotations(),
				rootPackage,
				pkg,
				globals);
		
		for (ParsedFile file : files) {
			file.registerTypes(moduleContext, rootPackage, pkg);
		}
		
		for (ParsedFile file : files) {
			// compileMembers will register all definition members to their
			// respective definitions, such as fields, constructors, methods...
			// It doesn't yet compile the method contents.
			try {
				file.compileTypes(moduleContext, rootPackage, pkg);
			} catch (CompileException ex) {
				exceptionLogger.accept(ex);
				failed = true;
			}
		}
		
		if (failed)
			return new SemanticModule(name, dependencies, SemanticModule.State.INVALID, rootPackage, pkg.getPackage(), definitions, Collections.emptyList(), registry.compilationUnit, expansions, registry.getAnnotations());
		
		// scripts will store all the script blocks encountered in the files
		PrecompilationState precompiler = new PrecompilationState();
		for (ParsedFile file : files) {
			file.registerMembers(moduleContext, precompiler, rootPackage, pkg, expansions, globals);
		}
		
		List<ScriptBlock> scripts = new ArrayList<>();
		for (ParsedFile file : files) {
			// compileCode will convert the parsed statements and expressions
			// into semantic code. This semantic code can then be compiled
			// to various targets.
			try {
				file.compileCode(moduleContext, precompiler, rootPackage, pkg, expansions, scripts, globals);
			} catch (CompileException ex) {
				exceptionLogger.accept(ex);
				failed = true;
			}
		}
		
		return new SemanticModule(name, dependencies, SemanticModule.State.ASSEMBLED, rootPackage, pkg.getPackage(), definitions, scripts, registry.compilationUnit, expansions, registry.getAnnotations());
	}
}
