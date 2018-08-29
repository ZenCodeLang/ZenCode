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

import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.compiler.CompilationUnit;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.formatter.FileFormatter;
import org.openzen.zenscript.formatter.ScriptFormattingSettings;
import org.openzen.zenscript.javabytecode.JavaCompiler;
import org.openzen.zenscript.javabytecode.JavaModule;
import org.openzen.zenscript.compiler.ModuleSpace;
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
		CompilingPackage compilingPkg = new CompilingPackage(pkg);
		ParsedFile[] parsedFiles = parse(compilingPkg, inputFiles);
		
		ZSPackage global = new ZSPackage(null, "");
		GlobalRegistry registry = new GlobalRegistry(global);
		SemanticModule module = compileSyntaxToSemantic(compilingPkg, parsedFiles, registry);
		
		//formatFiles(pkg, module);
		
		if (module.isValid()) {
			JavaModule javaModule = compileSemanticToJava(module);
			javaModule.execute();
		} else {
			System.out.println("There were compilation errors");
		}
    }
	
	private static ParsedFile[] parse(CompilingPackage compilingPkg, File[] files) throws IOException {
		ParsedFile[] parsedFiles = new ParsedFile[files.length];
		for (int i = 0; i < files.length; i++) {
			parsedFiles[i] = ParsedFile.parse(compilingPkg, new TestBracketParser(), files[i]);
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
	
	private static SemanticModule compileSyntaxToSemantic(CompilingPackage compiling, ParsedFile[] files, GlobalRegistry registry) {
		ModuleSpace space = new ModuleSpace(new CompilationUnit(), new ArrayList<>());
		for (Map.Entry<String, ISymbol> global : registry.collectGlobals().entrySet()) {
			space.addGlobal(global.getKey(), global.getValue());
		}
		SemanticModule result = ParsedFile.compileSyntaxToSemantic(
				"scripts",
				new SemanticModule[0],
				compiling,
				files,
				space,
				exception -> exception.printStackTrace());
		
		result = result.normalize();
		result.validate(entry -> {
			System.out.println(entry.kind + " " + entry.position.toString() + ": " + entry.message);
		});
		return result;
	}
	
	private static JavaModule compileSemanticToJava(SemanticModule module) {
		JavaCompiler compiler = new JavaCompiler(false, null);
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
