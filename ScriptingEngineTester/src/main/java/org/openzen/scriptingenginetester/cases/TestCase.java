package org.openzen.scriptingenginetester.cases;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.FileSource;
import org.junit.platform.engine.support.descriptor.UriSource;
import org.openzen.scriptingenginetester.TestOutput;
import org.openzen.zencode.shared.PathSourceFile;
import org.openzen.zencode.shared.SourceFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestCase {
	private final TestSource source;
	private final String name;
	private final List<SourceFile> sourceFiles = new ArrayList<>();
	private final TestAnnotations annotations;

	public TestCase(Path path) throws IOException {
		if(Files.isDirectory(path)) {
			throw new IllegalArgumentException("Multi-file tests are not yet supported");
		} else if(Files.isRegularFile(path)) {
			this.source = UriSource.from(path.toUri());
			String filename = path.getFileName().toString();
			this.name = withoutExtension(filename);

			PathSourceFile sourceFile = new PathSourceFile(filename, path);
			this.sourceFiles.add(sourceFile);
			annotations = TestAnnotations.extractFrom(sourceFile);
		} else {
			throw new IllegalArgumentException("Not a valid file or directory");
		}
	}

	public String getName() {
		return name;
	}

	public List<String> getRequiredStdLibModules() {
		return annotations.getDependencies();
	}

	public void validate(TestOutput output) {
		annotations.getAssertions().validate(output);
	}

    public List<SourceFile> getSourceFiles() {
		return sourceFiles;
    }

	public TestSource getSource() {
		return source;
	}

	private static String withoutExtension(String filename) {
		int index = filename.lastIndexOf('.');
		return index <= 0 ? filename : filename.substring(0, index);
	}
}
