package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.SharedGlobals;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class FunctionalInterfaceTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(FunctionalInterfaceTests.TestClass.class);
		requiredClasses.add(FunctionalInterfaceTests.StringModifier.class);
		requiredClasses.add(SharedGlobals.class);
		return requiredClasses;
	}

	@Override
	public List<String> getRequiredStdLibModules() {
		return Collections.singletonList("stdlib");
	}

	@Test
	public void testFunctionalInterface() {
		addScript(
				"var modified = modifyString('test', (strings, context) => { return strings; });\n" +
						"println(modified.length);",
						//"for str in modified { println(str); }",
				"FunctionalInterfaceTests_testFunctionalInterface.zs");

		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		//logger.assertPrintOutput(0, "test");
		logger.assertPrintOutput(0, "1");
	}

	@Test
	public void testBiFunction() {
		addScript(
				"var modified = stringFunction('test', (strings, context) => {return strings;});\n" +
						"println(modified.length);",
				//"for str in modified { println(str); }",
				"FunctionalInterfaceTests_testBiFunction.zs");

		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		//logger.assertPrintOutput(0, "test");
		logger.assertPrintOutput(0, "1");
	}

	@ZenCodeType.Name("test_module.java_native.TestClass")
	public static final class TestClass {
		@ZenCodeGlobals.Global
		public static List<String> modifyString(String baseString, StringModifier modifier) {
			return modifier.modify(Collections.singletonList(baseString), false);
		}

		@ZenCodeGlobals.Global
		public static List<String> stringFunction(String baseString, BiFunction<List<String>, Boolean, List<String>> function) {
			return function.apply(Collections.singletonList(baseString), false);
		}
	}

	@FunctionalInterface
	@ZenCodeType.Name("test_module.java_native.StringModifier")
	public static interface StringModifier {

		List<String> modify(List<String> strings, boolean context);
	}
}
