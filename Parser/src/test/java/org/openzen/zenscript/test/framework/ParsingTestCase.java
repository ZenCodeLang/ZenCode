package org.openzen.zenscript.test.framework;

import org.junit.jupiter.api.Assertions;
import org.openzen.zencode.shared.LiteralSourceFile;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.tree.Tree;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ParsingTestCase {
	private final Path zcFile;
	private final Path zastFile;

	public ParsingTestCase(Path zcFile, Path zastFile) {
		this.zcFile = zcFile;
		this.zastFile = zastFile;
	}

	public void execute() throws IOException {
		// Read file content
		try {
			if (Files.notExists(zcFile)) {
				Assertions.fail("File " + zcFile + " does not exist");
			}
			if (Files.notExists(zastFile)) {
				Assertions.fail("File " + zastFile + " does not exist");
			}
			String zcContent = new String(Files.readAllBytes(zcFile), StandardCharsets.UTF_8).replace("\r\n", "\n");
			String zastContent = new String(Files.readAllBytes(zastFile), StandardCharsets.UTF_8).replace("\r\n", "\n");
			Tree tree = Tree.parse(new LiteralSourceFile(zcFile.getFileName().toString(), zcContent));

			if (!zastContent.equals(tree.toString())) {
				Assertions.fail(
						"Error in \nfile://" + zcFile.toUri().getPath() + "\nfile://" + zastFile.toUri().getPath() + "\n Expected tree:\n'" + zastContent + "'\nbut got:\n'" + tree + "'\n"
				);
			}
		} catch (ParseException e) {
			Assertions.fail(e);
		}

	}

}