package org.openzen.scriptingenginetester.cases;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.FileSource;
import org.openzen.scriptingenginetester.TestOutput;
import org.openzen.zencode.shared.FileSourceFile;
import org.openzen.zencode.shared.SourceFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestCase {
	private final String name;
	private final TestSource source;
	private final List<SourceFile> sourceFiles = new ArrayList<>();
	private final TestAnnotations annotations;

	public TestCase(File file) throws IOException {
		this.name = withoutExtension(file.getName());
		this.source = FileSource.from(file);

		if (file.isDirectory()) {
			throw new IllegalArgumentException("Multi-file tests are not yet supported");
		} else if (file.isFile()) {
			FileSourceFile sourceFile = new FileSourceFile(file.getName(), file);
			sourceFiles.add(sourceFile);
			annotations = TestAnnotations.extractFrom(sourceFile);
		} else {
			throw new IllegalArgumentException("Not a valid file or directory");
		}
	}

	public TestCase(SourceFile sourceFile) throws IOException {
		this.name = withoutExtension(sourceFile.getFilename());
		this.source = new TestSource() {}; // IDEs won't be able to navigate to it
		sourceFiles.add(sourceFile);
		annotations = TestAnnotations.extractFrom(sourceFile);
	}

	public TestSource getSource() {
		return source;
	}

	public String getName() {
		return name;
	}

	public List<String> getRequiredStdLibModules() {
		return Collections.emptyList(); // TODO
	}

	public void validate(TestOutput output) {
		annotations.getAssertions().validate(output);
	}

    public List<SourceFile> getSourceFiles() {
		return sourceFiles;
    }

	private static String withoutExtension(String filename) {
		int index = filename.lastIndexOf('.');
		return index <= 0 ? filename : filename.substring(0, index);
	}
}
