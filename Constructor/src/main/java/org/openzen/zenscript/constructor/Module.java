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
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.constructor.module.ModuleSpace;
import org.openzen.zenscript.constructor.module.SemanticModule;
import org.openzen.zenscript.linker.symbol.ISymbol;
import org.openzen.zenscript.parser.ParsedFile;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;

/**
 *
 * @author Hoofdgebruiker
 */
public class Module {
	public final String name;
	public final File sourceDirectory;
	public final String packageName;
	public final String host;
	
	public Module(String name, File directory, File moduleFile) throws IOException {
		this.name = name;
		this.sourceDirectory = new File(directory, "src");
		
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(moduleFile));
		JSONObject json = new JSONObject(new JSONTokener(input));
		packageName = json.getString("package");
		host = json.getString("host");
	}
	
	public ParsedFile[] parse(ZSPackage pkg) throws IOException {
		List<ParsedFile> files = new ArrayList<>();
		parse(files, pkg, sourceDirectory);
		return files.toArray(new ParsedFile[files.size()]);
	}
	
	private void parse(List<ParsedFile> files, ZSPackage pkg, File directory) throws IOException {
		for (File file : directory.listFiles()) {
			if (file.getName().endsWith(".zs")) {
				try {
					files.add(ParsedFile.parse(pkg, file));
				} catch (CompileException ex) {
					System.out.println(ex.getMessage());
				}
			} else if (file.isDirectory()) {
				parse(files, pkg.getOrCreatePackage(file.getName()), file);
			}
		}
	}
	
	public static SemanticModule compileSyntaxToSemantic(ZSPackage pkg, ParsedFile[] files, ModuleSpace registry) {
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
		
		GlobalTypeRegistry globalRegistry = new GlobalTypeRegistry();
		for (ParsedFile file : files) {
			// compileMembers will register all definition members to their
			// respective definitions, such as fields, constructors, methods...
			// It doesn't yet compile the method contents.
			try {
				file.compileTypes(rootPackage, definitions, globalRegistry, expansions, globals);
			} catch (CompileException ex) {
				System.out.println(ex.getMessage());
				failed = true;
			}
		}
		
		if (failed)
			return new SemanticModule(false, pkg, definitions, Collections.emptyList());
		
		for (ParsedFile file : files) {
			// compileMembers will register all definition members to their
			// respective definitions, such as fields, constructors, methods...
			// It doesn't yet compile the method contents.
			try {
				file.compileMembers(rootPackage, definitions, globalRegistry, expansions, globals);
			} catch (CompileException ex) {
				System.out.println(ex.getMessage());
				failed = true;
			}
		}
		
		if (failed)
			return new SemanticModule(false, pkg, definitions, Collections.emptyList());
		
		// scripts will store all the script blocks encountered in the files
		List<ScriptBlock> scripts = new ArrayList<>();
		for (ParsedFile file : files) {
			// compileCode will convert the parsed statements and expressions
			// into semantic code. This semantic code can then be compiled
			// to various targets.
			try {
				file.compileCode(rootPackage, definitions, globalRegistry, expansions, scripts, globals);
			} catch (CompileException ex) {
				System.out.println(ex.getMessage());
				failed = true;
			}
		}
		
		if (failed)
			return new SemanticModule(false, pkg, definitions, Collections.emptyList());
		
		Validator validator = new Validator();
		boolean isValid = true;
		for (ScriptBlock script : scripts) {
			isValid &= validator.validate(script);
		}
		for (HighLevelDefinition definition : definitions.getAll()) {
			isValid &= validator.validate(definition);
		}
		
		for (ValidationLogEntry entry : validator.getLog()) {
			System.out.println(entry.kind + " " + entry.position.toString() + ": " + entry.message);
		}
		
		return new SemanticModule(isValid, pkg, definitions, scripts);
	}
}
