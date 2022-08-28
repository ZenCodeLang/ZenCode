package org.openzen.zenscript.parser;

import org.openzen.zencode.shared.LiteralSourceFile;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.ModuleSpace;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.logger.ParserLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FolderPackage {

	private final Map<String, List<SourceFile>> files = new HashMap<>();

	public FolderPackage(File file) {
		ArrayList<File> foundFiles = new ArrayList<>();
		getFiles(file, foundFiles);

		String srcString = File.separator + "src" + File.separator;
		foundFiles.stream().filter(fil -> (fil.getPath() + File.separator).substring(file.getPath().length() + File.separator.length()).indexOf(srcString) > 0).forEach(fil -> {
			String name = (fil.getPath() + File.separator).substring(file.getPath().length() + File.separator.length());
			int slash = name.indexOf(srcString);
			String moduleName = name.substring(0, slash);
			String filename = name.substring(slash + srcString.length());
			if (!files.containsKey(moduleName))
				files.put(moduleName, new ArrayList<>());

			try {
				if (!fil.isDirectory())
					files.get(moduleName).add(new LiteralSourceFile(filename, new BufferedReader(new FileReader(fil)).lines().collect(Collectors.joining("\n"))));
				else
					files.get(moduleName).add(new LiteralSourceFile(filename, ""));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		});
	}

	public void getFiles(File parent, List<File> files) {
		if (parent.isDirectory()) {
			for (File file : parent.listFiles()) {
				if (file.isDirectory()) {
					getFiles(file, files);
				}
			}
		}
		files.add(parent);
	}

	public SemanticModule loadModule(ModuleSpace space, String name, BracketExpressionParser bracketParser, SemanticModule[] dependencies, FunctionParameter[] scriptParameters, ParserLogger logger) throws ParseException {
		return loadModule(space, name, bracketParser, dependencies, scriptParameters, new ZSPackage(space.rootPackage, name), logger);
	}

	public SemanticModule loadModule(ModuleSpace space, String name, BracketExpressionParser bracketParser, SemanticModule[] dependencies, FunctionParameter[] scriptParameters, ZSPackage pkg, ParserLogger logger) throws ParseException {
		List<SourceFile> sourceFiles = files.get(name);
		if (sourceFiles == null)
			return null; // no such module

		ModuleSymbol scriptModule = new ModuleSymbol(name);
		CompilingPackage scriptPackage = new CompilingPackage(pkg, scriptModule);
		ParsedFile[] files = new ParsedFile[sourceFiles.size()];
		for (int i = 0; i < files.length; i++)
			files[i] = ParsedFile.parse(bracketParser, sourceFiles.get(i));

		SemanticModule scripts = ParsedFile.compileSyntaxToSemantic(
				dependencies,
				scriptPackage,
				files,
				space,
				scriptParameters,
				logger);
		return scripts.normalize();
	}
}
