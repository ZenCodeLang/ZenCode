package org.openzen.zenscript.scriptingexample.tests.actual_test.file_names;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

class FileNameWithDigitsTests extends ZenCodeTest {

	@Test
	void TestThatAFileMayConsistOnlyOfDigits() {
		ScriptBuilder.create()
				.startNewScript("123456789.zs")
				.add("println('Hello World');")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello World");
	}

	@Test
	void TestThatAFileMayConsistDotsAndDigits() {
		ScriptBuilder.create()
				.startNewScript("12345678.9.zs")
				.add("println('Hello World');")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello World");
	}
}
