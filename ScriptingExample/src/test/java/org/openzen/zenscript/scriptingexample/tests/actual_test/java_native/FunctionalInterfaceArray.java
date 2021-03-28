package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

public class FunctionalInterfaceArray extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(TestClass.class);
		return requiredClasses;
	}

	@Test
	public void testFunctionalInterfaceArray() {
		addScript("execute([() => println('A'), () => println('B')]);");
		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(2);
		logger.assertPrintOutput(0, "A");
		logger.assertPrintOutput(1, "B");
	}

	@ZenCodeType.Name("test_module.java_native.TestClass")
	public static final class TestClass {
		@ZenCodeGlobals.Global
		public static void execute(Runnable[] code) {
			for (Runnable r : code)
				r.run();
		}
	}
}
