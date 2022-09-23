package org.openzen.scriptingenginetester.cases;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.FileSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class TestGroup {
	private final String name;
	private final TestSource source;
	private final List<TestCase> cases = new ArrayList<>();

	public TestGroup(File directory) throws IOException {
		this.name = directory.getName();
		for (File file : directory.listFiles()) {
			cases.add(new TestCase(file));
		}
		this.source = FileSource.from(directory);
	}

	public TestGroup(String name, List<TestCase> cases) {
		this.name = name;
		this.source = new TestSource() {};
		this.cases.addAll(cases);
	}

	public TestSource getSource() {
		return source;
	}

	public String getName() {
		return name;
	}

	public List<TestCase> getCases() {
		return cases;
	}
}
