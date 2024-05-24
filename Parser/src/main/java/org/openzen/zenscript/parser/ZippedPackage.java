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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZippedPackage {
	private final Map<String, List<SourceFile>> files = new HashMap<>();

	public ZippedPackage(InputStream input) throws IOException {
		try (ZipInputStream zipInput = new ZipInputStream(new BufferedInputStream(input))) {
			ZipEntry entry = zipInput.getNextEntry();
			while (entry != null) {
				int slash = entry.getName().indexOf("/src/");
				if (slash > 0) {
					String moduleName = entry.getName().substring(0, slash);
					String filename = entry.getName().substring(slash + 5);
					if (!files.containsKey(moduleName))
						files.put(moduleName, new ArrayList<>());

					byte[] data = new byte[(int) entry.getSize()];
					int read = 0;
					while (read < data.length)
						read += zipInput.read(data, read, data.length - read);

					files.get(moduleName).add(new LiteralSourceFile(filename, new String(data, StandardCharsets.UTF_8)));
				}

				zipInput.closeEntry();
				entry = zipInput.getNextEntry();
			}
		}
	}

	public SemanticModule loadModule(ModuleSpace space, String name, BracketExpressionParser bracketParser, SemanticModule[] dependencies, FunctionParameter[] scriptParameters, ZSPackage pkg, ParserLogger logger) throws ParseException {
		List<SourceFile> sourceFiles = files.get(name);
		if (sourceFiles == null)
			return null; // no such module

		ModuleSymbol scriptModule = new ModuleSymbol(name);
		CompilingPackage scriptPackage = new CompilingPackage(pkg, scriptModule);
		ParsedFile[] files = new ParsedFile[sourceFiles.size()];
		for (int i = 0; i < files.length; i++)
			files[i] = ParsedFile.parse(scriptPackage.getPackageForSourceFile(sourceFiles.get(i)), bracketParser, sourceFiles.get(i));

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
