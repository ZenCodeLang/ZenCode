package org.openzen.scriptingenginetester.cases;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class TestGroup {
	private final File directory;
	private final List<TestCase> cases = new ArrayList<>();

	public TestGroup(File directory) throws IOException {
		this.directory = directory;
		for (File file : directory.listFiles()) {
			cases.add(new TestCase(file));
		}
	}

	public File getFile() {
		return directory;
	}

	public String getName() {
		return directory.getName();
	}

	public List<TestCase> getCases() {
		return cases;
	}
}
