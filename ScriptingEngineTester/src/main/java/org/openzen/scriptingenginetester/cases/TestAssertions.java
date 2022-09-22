package org.openzen.scriptingenginetester.cases;

import org.junit.jupiter.api.Assertions;
import org.openzen.scriptingenginetester.TestOutput;
import org.openzen.zencode.shared.CompileExceptionCode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TestAssertions {
	private final List<String> expectedOutput;
	private final List<ExpectedError> expectedErrors;

	private TestAssertions(List<String> expectedOutput, List<ExpectedError> expectedErrors) {
		this.expectedOutput = expectedOutput;
		this.expectedErrors = expectedErrors;
	}

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

	public void validate(TestOutput output) {
		validateOutput(output);
		validateErrors(output);
	}

	private void validateErrors(TestOutput output) {
		final String format = "%s:%s %s"; // file:line error-name
		final Stream<String> expectedErrors = this.expectedErrors.stream()
				.map(expectedError -> String.format(
						format,
						expectedError.filename,
						expectedError.line,
						expectedError.error.name())
				);

		final Stream<String> actualErrors = output.exceptions.stream()
				.map(actualError -> String.format(
						format,
						actualError.getPosition().getFilename(),
						actualError.getPosition().getFromLine(),
						actualError.error.code.name())
				);
		Assertions.assertLinesMatch(expectedErrors, actualErrors, "Test may only throw errors what were specified as '#error:' Preprocessors");
	}

	private void validateOutput(TestOutput output) {
		Assertions.assertLinesMatch(expectedOutput, output.output, "Test may only return what was added in '#output:' Preprocessors");
	}
}
