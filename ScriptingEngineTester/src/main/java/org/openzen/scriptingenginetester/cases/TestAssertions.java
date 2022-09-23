package org.openzen.scriptingenginetester.cases;

import org.junit.jupiter.api.Assertions;
import org.openzen.scriptingenginetester.TestOutput;
import org.openzen.zencode.shared.CompileExceptionCode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TestAssertions {
	private final List<String> expectedOutput;
	private final List<ExpectedError> expectedErrors;

	public TestAssertions(List<String> expectedOutput, List<ExpectedError> expectedErrors) {
		this.expectedOutput = expectedOutput;
		this.expectedErrors = expectedErrors;
	}

	public void validate(TestOutput output) {
		validateOutput(output);
		validateErrors(output);

		// ToDo: Do we want to call them at once like below, or one after another like above?
		//   Will we ever have a case where both validateOutput and validateErrors mismatch at once?
		//   If so, then assertAll would return both errors, whereas running them sequentially would only return
		//   The first error that was encountered.
		////Assertions.assertAll(
		////		() -> validateOutput(output),
		////		() -> validateErrors(output)
		////);
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
