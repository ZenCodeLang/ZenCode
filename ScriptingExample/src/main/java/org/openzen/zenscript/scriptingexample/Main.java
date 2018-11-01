package org.openzen.zenscript.scriptingexample;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;

import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.type.storage.StorageType;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.formatter.FileFormatter;
import org.openzen.zenscript.formatter.ScriptFormattingSettings;
import org.openzen.zenscript.javabytecode.JavaCompiler;
import org.openzen.zenscript.javabytecode.JavaModule;
import org.openzen.zenscript.compiler.ModuleSpace;
import org.openzen.zenscript.compiler.ZenCodeCompilingModule;
import org.openzen.zenscript.lexer.ParseException;
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
    public static void main(String[] args) throws ParseException {
		
		System.out.println();
		File inputDirectory = new File("scripts");
		File[] inputFiles = Optional.ofNullable(inputDirectory.listFiles((dir, name) -> name.endsWith(".zs"))).orElseGet(() -> new File[0]);
		
		ZSPackage pkg = new ZSPackage(null, "");
		Module module = new Module("scripts");
		CompilingPackage compilingPkg = new CompilingPackage(pkg, module);
		ParsedFile[] parsedFiles = parse(compilingPkg, inputFiles);
		
		ZSPackage root = ZSPackage.createRoot();
		ZSPackage stdlib = new ZSPackage(root, "stdlib");
		GlobalTypeRegistry typeRegistry = new GlobalTypeRegistry(stdlib);
		
		ZSPackage global = new ZSPackage(null, "");
		GlobalRegistry registry = new GlobalRegistry(typeRegistry, global);
		SemanticModule semantic = compileSyntaxToSemantic(typeRegistry, compilingPkg, parsedFiles, registry);
		
		//formatFiles(pkg, module);
		
		if (semantic.isValid()) {
			JavaModule javaModule = compileSemanticToJava(registry, semantic);
			javaModule.execute();
		} else {
			System.out.println("There were compilation errors");
		}
    }
	
	private static ParsedFile[] parse(CompilingPackage compilingPkg, File[] files) throws ParseException {
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
	
	private static SemanticModule compileSyntaxToSemantic(GlobalTypeRegistry typeRegistry, CompilingPackage compiling, ParsedFile[] files, GlobalRegistry registry) {
		ModuleSpace space = new ModuleSpace(typeRegistry, new ArrayList<>(), StorageType.getStandard());
		for (Map.Entry<String, ISymbol> global : registry.collectGlobals().entrySet()) {
			space.addGlobal(global.getKey(), global.getValue());
		}
		SemanticModule result = ParsedFile.compileSyntaxToSemantic(
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
	
	private static JavaModule compileSemanticToJava(GlobalRegistry registry, SemanticModule module) {
		JavaCompiler compiler = new JavaCompiler(module.registry, false, null);
		registry.register(compiler.getContext());
		
		ZenCodeCompilingModule compiling = compiler.addModule(module);
		for (HighLevelDefinition definition : module.definitions.getAll()) {
			compiling.addDefinition(definition);
		}
		for (ScriptBlock script : module.scripts) {
			compiling.addScriptBlock(script);
		}
		compiling.finish();
		
		return compiler.finishAndGetModule();
	}
	
	private static class TestBracketParser implements BracketExpressionParser {
		@Override
		public ParsedExpression parse(CodePosition position, ZSTokenParser tokens) throws ParseException {
			StringBuilder result = new StringBuilder();
			while (tokens.optional(ZSTokenType.T_GREATER) == null) {
				ZSToken token = tokens.next();
				result.append(token.content);
				result.append(tokens.getLastWhitespace());
			}
			
			return new ParsedExpressionString(position.until(tokens.getPosition()), result.toString(), false);
		}
	}
}
