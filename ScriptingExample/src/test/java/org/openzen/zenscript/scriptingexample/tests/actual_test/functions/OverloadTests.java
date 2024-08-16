package org.openzen.zenscript.scriptingexample.tests.actual_test.functions;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.SharedGlobals;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.Arrays;
import java.util.List;

class OverloadTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(GlobalPrinter.class);
		return requiredClasses;
	}

	@Test
	void testArrayOverloads() {
		ScriptBuilder.create()
				.add("printArray([1, 2, 3]);")
				.add("printArray(['one', 'two', 'three']);")
				.execute(this);

		logger.assertPrintOutputSize(2);
		logger.assertPrintOutput(0, "Numbers: [1, 2, 3]");
		logger.assertPrintOutput(1, "Strings: [one, two, three]");
	}

	@ZenCodeType.Name("test_module.GlobalPrinter")
	public static final class GlobalPrinter {
		@ZenCodeGlobals.Global
		public static void printArray(int[] numbers) {
			SharedGlobals.println("Numbers: " + Arrays.toString(numbers));
		}
		@ZenCodeGlobals.Global
		public static void printArray(String[] strings) {
			SharedGlobals.println("Strings: " + Arrays.toString(strings));
		}
	}

}