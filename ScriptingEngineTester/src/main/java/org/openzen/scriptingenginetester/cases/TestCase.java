package org.openzen.scriptingenginetester.cases;

import org.openzen.scriptingenginetester.TestException;
import org.openzen.scriptingenginetester.TestOutput;
import org.openzen.zencode.shared.FileSourceFile;
import org.openzen.zencode.shared.LiteralSourceFile;
import org.openzen.zencode.shared.SourceFile;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestCase {
	private final File file;
	private final List<SourceFile> sourceFiles = new ArrayList<>();
	private final TestAssertions assertions;

	public TestCase(File file) throws IOException {
		this.file = file;

		if (file.isDirectory()) {
			throw new IllegalArgumentException("Multi-file tests are not yet supported");
		} else if (file.isFile()) {
			sourceFiles.add(new FileSourceFile(file.getName(), file));
			assertions = TestAssertions.extractFrom(file);
		} else {
			throw new IllegalArgumentException("Not a valid file or directory");
		}
	}

	public File getFile() {
		return file;
	}

	public String getName() {
		return file.getName();
	}

	public List<String> getRequiredStdLibModules() {
		return Collections.emptyList(); // TODO
	}

	public void validate(TestOutput output) {
		assertions.validate(output);
	}

    public List<SourceFile> getSourceFiles() {
		return sourceFiles;
    }
}
