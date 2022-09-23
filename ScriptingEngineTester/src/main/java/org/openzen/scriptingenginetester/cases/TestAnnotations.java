package org.openzen.scriptingenginetester.cases;

import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zencode.shared.SourceFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestAnnotations {
	public static TestAnnotations extractFrom(SourceFile file) throws IOException {
		List<String> expectedOutput = new ArrayList<>();
		List<ExpectedError> expectedErrors = new ArrayList<>();
		List<String> dependencies = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(file.open())) {
			int[] lineNumber = new int[] { 0 };
			reader.lines().forEach(line -> {
				lineNumber[0]++;
				line = line.trim();
				if (!line.startsWith("#"))
					return;

				String[] parts = line.substring(1).split(":", 2);
				switch (parts[0]) {
					case "output":
						expectedOutput.add(parts[1].trim());
						break;
					case "error":
						expectedErrors.add(parseError(file.getFilename(), parts[1].trim()));
						break;
					case "dependency":
						dependencies.add(parts[1].trim());
						break;
				}
			});
		}

		return new TestAnnotations(expectedOutput, expectedErrors, dependencies);
	}

	private static ExpectedError parseError(String filename, String errorSpecification) {
		String[] parts = errorSpecification.split(":", 2);
		try {
			return new ExpectedError(filename, Integer.parseInt(parts[0]), CompileExceptionCode.valueOf(parts[1]));
		} catch (NumberFormatException ex) {
			throw new RuntimeException("Invalid line number in " + filename + ": " + parts[0], ex);
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException("Invalid error code in " + filename + ": " + parts[1], ex);
		}
	}

	private final List<String> expectedOutput;
	private final List<ExpectedError> expectedErrors;
	private final List<String> dependencies;

	private TestAnnotations(List<String> expectedOutput, List<ExpectedError> expectedErrors, List<String> dependencies) {
		this.expectedOutput = expectedOutput;
		this.expectedErrors = expectedErrors;
		this.dependencies = dependencies;
	}

	public TestAssertions getAssertions() {
		return new TestAssertions(expectedOutput, expectedErrors);
	}

	public List<String> getDependencies() {
		return dependencies;
	}
}
