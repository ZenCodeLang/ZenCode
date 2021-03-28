package org.openzen.zenscript.scriptingexample.tests.actual_test.lexer;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class UnicodeCharacters extends ZenCodeTest {
	@Test
	public void unicode() {
		addScript("println('你好');");
		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "你好");
	}
}
