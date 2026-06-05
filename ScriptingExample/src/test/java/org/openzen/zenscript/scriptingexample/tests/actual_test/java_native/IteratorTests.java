package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.SharedGlobals;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.*;

public class IteratorTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(TestIterable.class);
		requiredClasses.add(SharedGlobals.class);
		return requiredClasses;
	}

	@Override
	public List<String> getRequiredStdLibModules() {
		return Collections.singletonList("stdlib");
	}

	@Test
	void testFunctionalInterfaces() {
		addScript(
				"var source = new test_module.java_native.TestIterable(); for item in source { println(item); }",
				"IteratorTests_testFunctionalInterfaces.zs");

		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(2);
		logger.assertPrintOutput(0, "test");
		logger.assertPrintOutput(1, "instance");
	}

	@ZenCodeType.Name("test_module.java_native.TestIterable")
	public static class TestIterable implements Iterable<String> {

		@ZenCodeType.Constructor
		public TestIterable() {
		}

		@Override
		public Iterator<String> iterator() {
			return Arrays.asList("test", "instance").iterator();
		}
	}
}
