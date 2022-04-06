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

	@Test
	public void testGenericParameterBoxing() {
		addScript(
				"public class Duad<A, B> {\n" +
				"    public var a as A : get;\n" +
				"    public var b as B : get;\n" +
				"    public this(a as A, b as B) {\n" +
				"        this.a = a;\n" +
				"        this.b = b;\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"public function duadExample(name as string, age as int) as Duad<string, int> {\n" +
				"    return new Duad<string, int>(name, age);\n" +
				"}\n" +
				"\n" +
				"var jimmy = duadExample(\"Jimmy\", 19);\n" +
				"\n" +
				"println(jimmy.a);" +
				"println(jimmy.b);"
		);
		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(2);
		logger.assertPrintOutput(0, "Jimmy");
		logger.assertPrintOutput(1, "19");
	}
}
