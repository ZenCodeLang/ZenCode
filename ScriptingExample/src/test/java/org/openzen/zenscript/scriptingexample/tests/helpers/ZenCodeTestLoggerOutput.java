package org.openzen.zenscript.scriptingexample.tests.helpers;

import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZenCodeTestLoggerOutput {

	protected final List<String> lines = new ArrayList<>();

	void add(String line) {
		lines.addAll(Arrays.asList(String.valueOf(line).split(System.lineSeparator())));
	}

	public void assertEmpty() {
		assertSize(0);
	}

	public void assertNotEmpty() {
		Assertions.assertNotEquals(0, lines.size());
	}

	public void assertSize(int size) {
		Assertions.assertEquals(size, lines.size());
	}

	public void assertLine(int line, String content) {
		Assertions.assertEquals(content, lines.get(line));
	}

	public void assertLineContains(int line, String content) {
		final String foundLine = lines.get(line);
		Assertions.assertTrue(foundLine.contains(content), "Expected this line to contain '" + content + "' but found '" + line + "'!");
	}

	public void assertLinesInOrder(String... lines) {
		Assertions.assertEquals(Arrays.asList(lines), this.lines);
	}

	public void assertLinesPresentInAnyOrder(String... lines) {
		for (String line : lines) {
			Assertions.assertTrue(this.lines.contains(line));
		}
	}
}
