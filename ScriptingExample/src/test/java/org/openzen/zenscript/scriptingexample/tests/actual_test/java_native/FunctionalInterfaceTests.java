package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.SharedGlobals;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

class FunctionalInterfaceTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(FunctionalInterfaceTests.TestClass.class);
		requiredClasses.add(FunctionalInterfaceTests.StringModifier.class);
		requiredClasses.add(FunctionalInterfaceTests.IntModifier.class);
		requiredClasses.add(SharedGlobals.class);
		requiredClasses.add(Test2.class);
		return requiredClasses;
	}

	@Override
	public List<String> getRequiredStdLibModules() {
		return Collections.singletonList("stdlib");
	}

	@Test
	void testFunctionalInterfaces() {
		addScript(
				"var modified = modifyString('test', (strings, context) => { return straightUpItself(strings); });\n" +
						"println(modified.length);",
				"FunctionalInterfaceTests_testFunctionalInterface.zs");

		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "1");
	}

	@Test
	void testFunctionalInterface() {
		addScript(
				"var modified = modifyStrings([test_module.java_native.Test.instance()]);\n" +
						"println(modified.length);",
				"FunctionalInterfaceTests_testFunctionalInterfaces.zs");

		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "0");
	}

	@Test
	void testFunctionalInterfacesAreDifferent() {
		addScript(
				"var modified = modifyInts([test_module.java_native.Test.instance()]);\n",
				"FunctionalInterfaceTests_testFunctionalInterfaces.zs");

		executeEngine(true);
		logger.assertHasErrors();
		logger.errors().assertLineContains(0, "Cannot implicitly cast Test to function(arg0: List<int?>, arg1: bool): List<int?>");
	}

	@Test
	void testBiFunction() {
		addScript(
				"var modified = stringFunction('test', (strings, context) => { return straightUpItself(strings); });\n" +
						"println(modified.length);",
				"FunctionalInterfaceTests_testBiFunction.zs");

		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "1");
	}

	@ZenCodeType.Name("test_module.java_native.TestClass")
	public static final class TestClass {
		@ZenCodeGlobals.Global
		public static List<String> modifyString(String baseString, StringModifier modifier) {
			return modifier.modify(Collections.singletonList(baseString), false);
		}

		@ZenCodeGlobals.Global
		public static List<String> modifyStrings(StringModifier... modifier) {
			return new ArrayList<>();
		}

		@ZenCodeGlobals.Global
		public static List<String> modifyInts(IntModifier... modifier) {
			return new ArrayList<>();
		}

		@ZenCodeGlobals.Global
		public static List<String> stringFunction(String baseString, BiFunction<List<String>, Boolean, List<String>> function) {
			return function.apply(Collections.singletonList(baseString), false);
		}

		@ZenCodeGlobals.Global
		public static List<String> straightUpItself(List<String> list) {
			return list;
		}
	}

	@FunctionalInterface
	@ZenCodeType.Name("test_module.java_native.StringModifier")
	public interface StringModifier {

		List<String> modify(List<String> strings, boolean context);
	}

	@FunctionalInterface
	@ZenCodeType.Name("test_module.java_native.IntModifier")
	public interface IntModifier {
		List<Integer> modify(List<Integer> ints, boolean context);
	}

	@ZenCodeType.Name("test_module.java_native.Test")
	public static class Test2 implements StringModifier {

		@ZenCodeType.Constructor
		public Test2() {
		}

		@ZenCodeType.Method
		public static Test2 instance() {
			return new Test2();
		}

		@Override
		public List<String> modify(List<String> strings, boolean context) {
			return Collections.emptyList();
		}
	}

}
