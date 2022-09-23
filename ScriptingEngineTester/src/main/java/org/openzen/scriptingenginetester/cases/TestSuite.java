package org.openzen.scriptingenginetester.cases;

import org.openzen.zencode.shared.LiteralSourceFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

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

	public TestSuite(ZipFile zipFile, String path) {
		String prefix = path + "/";
		Map<String, List<TestCase>> casesByGroup = new HashMap<>();
		zipFile.stream().forEach(file -> {
			if (!file.getName().startsWith(prefix))
				return;
			if (file.isDirectory())
				return;

			String[] parts = file.getName().substring(prefix.length()).split("/");
			if (parts.length == 2) {
				try {
					String content = new BufferedReader(new InputStreamReader(zipFile.getInputStream(file)))
							.lines()
							.collect(Collectors.joining("\n"));
					casesByGroup.computeIfAbsent(parts[0], x -> new ArrayList<>()).add(new TestCase(new LiteralSourceFile(parts[1], content)));
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			} else {
				throw new IllegalArgumentException("Multi-file tests are not yet supported");
			}
		});

		for (Map.Entry<String, List<TestCase>> entry : casesByGroup.entrySet()) {
			groups.add(new TestGroup(entry.getKey(), entry.getValue()));
		}
	}

	public List<TestGroup> getGroups() {
		return groups;
	}
}
