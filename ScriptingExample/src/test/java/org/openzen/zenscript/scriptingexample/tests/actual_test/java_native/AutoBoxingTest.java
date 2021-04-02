package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class AutoBoxingTest extends ZenCodeTest {
	@Test
	public void testIndexSetCorrectlyBoxesKeysAndValues() {
		addScript(
				"var item_values = {} as int[string];\n" +
				"item_values[\"box\"] = 1;\n" +
				"item_values[\"test\"] = 9;\n" +
				"println(item_values[\"box\"]);\n" +
				"\n" +
				"var value_items = {} as string[int];\n" +
				"value_items[1] = \"box\";\n" +
				"value_items[9] = \"test\";\n" +
				"println(value_items[9]);"
		);
		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(2);
		logger.assertPrintOutput(0, Integer.toString(1));
		logger.assertPrintOutput(1, "test");
	}
}
