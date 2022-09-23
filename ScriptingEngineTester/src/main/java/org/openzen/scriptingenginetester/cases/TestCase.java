package org.openzen.scriptingenginetester.cases;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.FileSource;
import org.junit.platform.engine.support.descriptor.UriSource;
import org.openzen.scriptingenginetester.TestException;
import org.openzen.scriptingenginetester.TestOutput;
import org.openzen.zencode.shared.FileSourceFile;
import org.openzen.zencode.shared.LiteralSourceFile;
import org.openzen.zencode.shared.PathSourceFile;
import org.openzen.zencode.shared.SourceFile;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestCase {
	private final TestSource source;
	private final String name;
	private final List<SourceFile> sourceFiles = new ArrayList<>();
	private final TestAssertions assertions;

	public TestCase(File file) throws IOException {
		this.source = FileSource.from(file);
		this.name = file.getName();

		if (file.isDirectory()) {
			throw new IllegalArgumentException("Multi-file tests are not yet supported");
		} else if (file.isFile()) {
			sourceFiles.add(new FileSourceFile(file.getName(), file));
			assertions = TestAssertions.extractFrom(file);
		} else {
			throw new IllegalArgumentException("Not a valid file or directory");
		}
	}

	public TestCase(Path path) throws IOException {
		if(Files.isDirectory(path)) {
			throw new IllegalArgumentException("Multi-file tests are not yet supported");
		} else if(Files.isRegularFile(path)) {
			this.source = UriSource.from(path.toUri());
			this.name = path.getFileName().toString();
			this.assertions = TestAssertions.extractFrom(path);
			this.sourceFiles.add(new PathSourceFile(name, path));
		} else {
			throw new IllegalArgumentException("Not a valid file or directory");
		}
	}


	public String getName() {
		return name;
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

	public TestSource getSource() {
		return source;
	}
}
