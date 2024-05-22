package org.openzen.scriptingenginetester.cases;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.UriSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public final class TestGroup {
	private final TestSource directory;
	private final String name;
	private final List<TestCase> cases = new ArrayList<>();

	public TestGroup(Path dir) {
		this.directory = UriSource.from(dir.toUri());
		this.name = dir.getFileName().toString();
		try (Stream<Path> paths = Files.list(dir)) {
			for(Iterator<Path> iterator = paths.iterator(); iterator.hasNext(); ) {
				final Path next = iterator.next();
				cases.add(new TestCase(dir, next));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	public TestSource getSource () {
		return directory;
	}

	public String getName() {
		return name;
	}

	public List<TestCase> getCases() {
		return cases;
	}
}
