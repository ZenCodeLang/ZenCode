package org.openzen.zenscript.scriptingexample.tests.actual_test.strings;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class StringBuiltinOperatorTests extends ZenCodeTest {
	@Test
	public void toUpperCaseMethod() {
		ScriptBuilder.create()
				.add("println('HellO World'.toUpperCase());")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello World".toUpperCase());
	}

	@Test
	public void toLowerCaseMethod() {
		ScriptBuilder.create()
				.add("println('HellO World'.toLowerCase());")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "HellO World".toLowerCase());
	}
}
