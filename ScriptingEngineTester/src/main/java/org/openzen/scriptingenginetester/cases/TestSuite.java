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

		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				groups.add(new TestGroup(file));
			}
		}
	}

	public List<TestGroup> getGroups() {
		return groups;
	}
}
