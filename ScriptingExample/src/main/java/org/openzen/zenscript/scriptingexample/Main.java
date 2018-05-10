package org.openzen.zenscript.scriptingexample;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openzen.zenscript.codemodel.HighLevelDefinition;

import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.formatter.FileFormatter;
import org.openzen.zenscript.formatter.FormattingSettings;
import org.openzen.zenscript.javabytecode.JavaCompiler;
import org.openzen.zenscript.javabytecode.JavaModule;
import org.openzen.zenscript.linker.symbol.ISymbol;
import org.openzen.zenscript.parser.ParsedFile;
import org.openzen.zenscript.shared.SourceFile;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;

public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
		System.out.println();
		File inputDirectory = new File("scripts");
		File[] inputFiles = Optional.ofNullable(inputDirectory.listFiles((dir, name) -> name.endsWith(".zs"))).orElseGet(() -> new File[0]);
		
		ZSPackage pkg = new ZSPackage("");
		ParsedFile[] parsedFiles = parse(pkg, inputFiles);
		
		ZSPackage global = new ZSPackage("");
		GlobalRegistry registry = new GlobalRegistry(global);
		SemanticModule module = compileSyntaxToSemantic(parsedFiles, registry);
		
		formatFiles(pkg, module);
		
		if (module.isValid) {
			JavaModule javaModule = compileSemanticToJava(module);
			javaModule.execute();
		} else {
			System.out.println("There were compilation errors");
		}
    }
	
	private static ParsedFile[] parse(ZSPackage pkg, File[] files) throws IOException {
		ParsedFile[] parsedFiles = new ParsedFile[files.length];
		for (int i = 0; i < files.length; i++) {
			parsedFiles[i] = ParsedFile.parse(pkg, files[i]);
		}
		return parsedFiles;
	}
	
	private static void formatFiles(ZSPackage pkg, SemanticModule module) {
		Map<String, FileContents> files = new HashMap<>();
		for (ScriptBlock block : module.scripts) {
			SourceFile file = block.getTag(SourceFile.class);
			if (file == null)
				continue;
			
			FileContents contents = new FileContents(file.filename);
			contents.script = block;
			files.put(file.filename, contents);
		}
		
		for (HighLevelDefinition definition : module.definitions.getAll()) {
			SourceFile file = definition.getTag(SourceFile.class);
			if (file == null)
				continue;
			
			if (!files.containsKey(file.filename))
				files.put(file.filename, new FileContents(file.filename));
			
			files.get(file.filename).definitions.add(definition);
		}
		
		List<String> filenames = new ArrayList<>(files.keySet());
		Collections.sort(filenames);
		
		FormattingSettings settings = new FormattingSettings.Builder().build();
		for (String filename : filenames) {
			FileContents contents = files.get(filename);
			FileFormatter formatter = new FileFormatter(settings);
			System.out.println("== " + filename + " ==");
			System.out.println(formatter.format(pkg, contents.script, contents.definitions));
		}
	}
	
	private static SemanticModule compileSyntaxToSemantic(ParsedFile[] files, GlobalRegistry registry) {
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
		Map<String, ISymbol> globals = registry.collectGlobals();
		
		GlobalTypeRegistry globalRegistry = new GlobalTypeRegistry();
		for (ParsedFile file : files) {
			// compileMembers will register all definition members to their
			// respective definitions, such as fields, constructors, methods...
			// It doesn't yet compile the method contents.
			file.compileMembers(rootPackage, definitions, globalRegistry, expansions, globals);
		}
		
		// scripts will store all the script blocks encountered in the files
		List<ScriptBlock> scripts = new ArrayList<>();
		for (ParsedFile file : files) {
			// compileCode will convert the parsed statements and expressions
			// into semantic code. This semantic code can then be compiled
			// to various targets.
			file.compileCode(rootPackage, definitions, globalRegistry, expansions, scripts, globals);
		}
		
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
		
		return new SemanticModule(isValid, definitions, scripts);
	}
	
	private static JavaModule compileSemanticToJava(SemanticModule module) {
		JavaCompiler compiler = new JavaCompiler(false);
		for (HighLevelDefinition definition : module.definitions.getAll()) {
			compiler.addDefinition(definition);
		}
		for (ScriptBlock script : module.scripts) {
			compiler.addScriptBlock(script);
		}
		return compiler.finish();
	}
}
