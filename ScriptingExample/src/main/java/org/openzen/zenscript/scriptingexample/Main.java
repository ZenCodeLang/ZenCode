package org.openzen.zenscript.scriptingexample;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.HighLevelDefinition;

import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.compiler.CompilationUnit;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.formatter.FileFormatter;
import org.openzen.zenscript.formatter.ScriptFormattingSettings;
import org.openzen.zenscript.javabytecode.JavaCompiler;
import org.openzen.zenscript.javabytecode.JavaModule;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.BracketExpressionParser;
import org.openzen.zenscript.parser.ParsedFile;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.expression.ParsedExpressionString;

public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
		System.out.println();
		File inputDirectory = new File("scripts");
		File[] inputFiles = Optional.ofNullable(inputDirectory.listFiles((dir, name) -> name.endsWith(".zs"))).orElseGet(() -> new File[0]);
		
		ZSPackage pkg = new ZSPackage(null, "");
		ParsedFile[] parsedFiles = parse(pkg, inputFiles);
		
		ZSPackage global = new ZSPackage(null, "");
		GlobalRegistry registry = new GlobalRegistry(global);
		SemanticModule module = compileSyntaxToSemantic(parsedFiles, registry);
		
		formatFiles(pkg, module);
		
		if (module.isValid()) {
			JavaModule javaModule = compileSemanticToJava(module);
			javaModule.execute();
		} else {
			System.out.println("There were compilation errors");
		}
    }
	
	private static ParsedFile[] parse(ZSPackage pkg, File[] files) throws IOException {
		ParsedFile[] parsedFiles = new ParsedFile[files.length];
		for (int i = 0; i < files.length; i++) {
			parsedFiles[i] = ParsedFile.parse(pkg, new TestBracketParser(), files[i]);
		}
		return parsedFiles;
	}
	
	private static void formatFiles(ZSPackage pkg, SemanticModule module) {
		Map<String, FileContents> files = new HashMap<>();
		for (ScriptBlock block : module.scripts) {
			SourceFile file = block.getTag(SourceFile.class);
			if (file == null)
				continue;
			
			FileContents contents = new FileContents(file);
			contents.script = block;
			files.put(file.getFilename(), contents);
		}
		
		for (HighLevelDefinition definition : module.definitions.getAll()) {
			SourceFile file = definition.getTag(SourceFile.class);
			if (file == null)
				continue;
			
			if (!files.containsKey(file.getFilename()))
				files.put(file.getFilename(), new FileContents(file));
			
			files.get(file.getFilename()).definitions.add(definition);
		}
		
		List<String> filenames = new ArrayList<>(files.keySet());
		Collections.sort(filenames);
		
		ScriptFormattingSettings settings = new ScriptFormattingSettings.Builder().build();
		for (String filename : filenames) {
			FileContents contents = files.get(filename);
			FileFormatter formatter = new FileFormatter(settings);
			System.out.println("== " + filename + " ==");
			System.out.println(formatter.format(pkg, contents.script, contents.definitions));
		}
	}
	
	private static SemanticModule compileSyntaxToSemantic(ParsedFile[] files, GlobalRegistry registry) {
		ZSPackage modulePackage = new ZSPackage(null, "");
		
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
		
		CompilationUnit compilationUnit = new CompilationUnit();
		ZSPackage rootPackage = registry.collectPackages();
		List<ExpansionDefinition> expansions = registry.collectExpansions();
		Map<String, ISymbol> globals = registry.collectGlobals();
		// TODO: load stdlib
		
		for (ParsedFile file : files) {
			// compileMembers will register all definition members to their
			// respective definitions, such as fields, constructors, methods...
			// It doesn't yet compile the method contents.
			file.compileMembers(rootPackage, modulePackage, definitions, compilationUnit.globalTypeRegistry, expansions, globals, Collections.emptyList());
		}
		
		// scripts will store all the script blocks encountered in the files
		List<ScriptBlock> scripts = new ArrayList<>();
		for (ParsedFile file : files) {
			// compileCode will convert the parsed statements and expressions
			// into semantic code. This semantic code can then be compiled
			// to various targets.
			file.compileCode(rootPackage, modulePackage, definitions, compilationUnit.globalTypeRegistry, expansions, scripts, globals, Collections.emptyList());
		}
		
		SemanticModule result = new SemanticModule(
				"scripts",
				new String[0],
				SemanticModule.State.SEMANTIC,
				rootPackage,
				modulePackage,
				definitions,
				scripts,
				compilationUnit,
				expansions,
				Collections.emptyList());
		
		result = result.normalize();
		result.validate(entry -> {
			System.out.println(entry.kind + " " + entry.position.toString() + ": " + entry.message);
		});
		return result;
	}
	
	private static JavaModule compileSemanticToJava(SemanticModule module) {
		JavaCompiler compiler = new JavaCompiler(false);
		for (HighLevelDefinition definition : module.definitions.getAll()) {
			compiler.addDefinition(definition, module);
		}
		for (ScriptBlock script : module.scripts) {
			compiler.addScriptBlock(script);
		}
		return compiler.finishAndGetModule();
	}
	
	private static class TestBracketParser implements BracketExpressionParser {
		@Override
		public ParsedExpression parse(CodePosition position, ZSTokenParser tokens) {
			StringBuilder result = new StringBuilder();
			while (tokens.optional(ZSTokenType.T_GREATER) == null) {
				ZSToken token = tokens.next();
				result.append(token.content);
				result.append(tokens.getLastWhitespace());
			}
			
			return new ParsedExpressionString(position.until(tokens.getPosition()), result.toString());
		}
	}
}
