package org.openzen.scriptingenginetester.cases;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestSuite {
	private final List<TestGroup> groups = new ArrayList<>();

	public TestSuite(File directory) throws IOException {
		if (!directory.isDirectory())
			throw new IllegalArgumentException("Not a valid directory");

		final File[] files = directory.listFiles();
		if(files == null || files.length == 0) {
			throw new IllegalStateException("No Tests found!");
		}

		for (File file : files) {
			if (file.isDirectory()) {
				groups.add(new TestGroup(file));
			}
		}
	}

	public List<TestGroup> getGroups() {
		return groups;
	}
}
