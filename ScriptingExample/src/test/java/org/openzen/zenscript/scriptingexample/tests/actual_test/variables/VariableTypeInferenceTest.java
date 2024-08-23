package org.openzen.zenscript.scriptingexample.tests.actual_test.variables;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

class VariableTypeInferenceTest extends ZenCodeTest {
	@Test
	void validateCast() {
		addScript("var a = 1; println(a); println(a * 2);");
		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(2);
		logger.assertPrintOutput(0, "1");
		logger.assertPrintOutput(1, "2");
	}

	@Disabled("not valid for now")
	@Test
	void validateInference() {
		addScript("var a; a = 1; println(a);");
		executeEngine(true);

		logger.assertHasErrors();
	}
}
