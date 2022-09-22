package org.openzen.scriptingenginetester.cases;

import org.junit.jupiter.api.Assertions;
import org.openzen.scriptingenginetester.TestException;
import org.openzen.scriptingenginetester.TestOutput;
import org.openzen.zencode.shared.CompileExceptionCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TestAssertions {
	private final List<String> expectedOutput;
	private final List<ExpectedError> expectedErrors;

	public static TestAssertions extractFrom(File file) throws IOException {
		List<String> expectedOutput = new ArrayList<>();
		List<ExpectedError> expectedErrors = new ArrayList<>();

		for (String line : Files.readAllLines(file.toPath())) {
			line = line.trim();
			if (line.startsWith("#")) {
				if (line.startsWith("#output: ")) {
					expectedOutput.add(line.substring("#output: ".length()));
				} else if (line.startsWith("#error: ")) {
					String[] parts = line.substring("#error: ".length()).split(":", 2);
					try {
						expectedErrors.add(new ExpectedError(file.getName(), Integer.parseInt(parts[0]), CompileExceptionCode.valueOf(parts[1])));
					} catch (NumberFormatException ex) {
						throw new RuntimeException("Invalid line number in " + file + ": " + parts[0], ex);
					} catch (IllegalArgumentException ex) {
						throw new RuntimeException("Invalid error code in " + file + ": " + parts[1], ex);
					}
				}
			}
		}

		return new TestAssertions(expectedOutput, expectedErrors);
	}

	private TestAssertions(List<String> expectedOutput, List<ExpectedError> expectedErrors) {
		this.expectedOutput = expectedOutput;
		this.expectedErrors = expectedErrors;
	}

	public void validate(TestOutput output) {
		Assertions.assertArrayEquals(expectedOutput.toArray(), output.output.toArray());

	}
}
