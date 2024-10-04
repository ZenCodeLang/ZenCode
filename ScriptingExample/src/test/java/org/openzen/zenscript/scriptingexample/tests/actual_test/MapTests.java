package org.openzen.zenscript.scriptingexample.tests.actual_test;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

class MapTests extends ZenCodeTest {
	@Test
	void keysWork() {
		ScriptBuilder.create()
				.add("var map = {'Hello': 'World'};")
				.add("println(map.keys.length);")
				.add("println(map.keys[$-1]);")
				.execute(this);

		logger.assertPrintOutputSize(2);
		logger.assertPrintOutput(0, "1");
		logger.assertPrintOutput(1, "Hello");
	}

	@Test
	void valuesWork() {
		ScriptBuilder.create()
				.add("var map = {'Hello': 'World'};")
				.add("println(map.values.length);")
				.add("println(map.values[$-1]);")
				.execute(this);

		logger.assertPrintOutputSize(2);
		logger.assertPrintOutput(0, "1");
		logger.assertPrintOutput(1, "World");
	}
}
