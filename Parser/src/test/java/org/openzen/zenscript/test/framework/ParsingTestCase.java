package org.openzen.zenscript.test.framework;

import org.junit.jupiter.api.Assertions;
import org.openzen.zencode.shared.LiteralSourceFile;
import org.openzen.zenscript.tree.ParseError;
import org.openzen.zenscript.tree.Tree;
import org.openzen.zenscript.tree.ast.CSTToASTVisitor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ParsingTestCase {
	private final Path zcFile;
	private final Path zastFile;

	public ParsingTestCase(Path zcFile, Path zastFile) {
		this.zcFile = zcFile;
		this.zastFile = zastFile;
	}

	public void execute() throws IOException {
		// Read file content
		if (Files.notExists(zcFile)) {
			Assertions.fail("File " + zcFile + " does not exist");
		}
		if (Files.notExists(zastFile)) {
			Assertions.fail("File " + zastFile + " does not exist");
		}
		String zcContent = new String(Files.readAllBytes(zcFile), StandardCharsets.UTF_8).replace("\r\n", "\n");
		String zastContent = new String(Files.readAllBytes(zastFile), StandardCharsets.UTF_8).replace("\r\n", "\n");
		LiteralSourceFile sourceFile = new LiteralSourceFile(zcFile.getFileName().toString(), zcContent);
		Tree tree = Tree.parse(sourceFile);

		List<ParseError> errors = new ArrayList<>();
		tree.reportErrors(errors);
		if(errors.isEmpty()) {
			CSTToASTVisitor cstToASTVisitor = new CSTToASTVisitor(sourceFile);

			tree.accept(cstToASTVisitor, null);
		}
		if (!zastContent.equals(tree.toString())) {
			Assertions.fail(
					"Error in \nfile://" + zcFile.toUri().getPath() + "\nfile://" + zastFile.toUri().getPath() + "\n Expected tree:\n'" + zastContent + "'\nbut got:\n'" + tree + "'\n"
			);
		}

	}

}