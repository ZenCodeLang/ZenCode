package org.openzen.scriptingenginetester.cases;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TestSuite {
	private final List<TestGroup> groups = new ArrayList<>();

	public TestSuite(Path directory) {
		if(!Files.isDirectory(directory)) {
			throw new IllegalArgumentException("Not a valid directory");
		}

		try(Stream<Path> paths = Files.list(directory)) {
			paths.filter(Files::isDirectory)
					.forEach(dir -> groups.add(new TestGroup(dir)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<TestGroup> getGroups() {
		return groups;
	}
}
