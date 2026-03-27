package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

public class RawTypeInHeaderTest extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(GenClass.class);
		requiredClasses.add(TestGlobals.class);
		return requiredClasses;
	}

	@Test
	void testRawTypeErrors1() {
		addScript(
				"foo1(new test_module.java_native.GenClass<string>());\n",
				"RawTypeInHeaderTest_testRawTypeErrors.zs");

		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
	}

	@Test
	void testRawTypeErrors2() {
		addScript(
				"foo2(new test_module.java_native.GenClass<string>());\n",
				"RawTypeInHeaderTest_testRawTypeErrors.zs");

		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
	}

	@ZenCodeType.Name("test_module.java_native.TestClass")
	public static final class TestGlobals {
		@ZenCodeGlobals.Global
		public static void foo1(GenClass<String> clazz) {

		}
		@ZenCodeGlobals.Global
		public static void foo2(GenClass clazz) {

		}
	}

	@ZenCodeType.Name("test_module.java_native.GenClass")
	public static final class GenClass<T> {
		@ZenCodeType.Constructor
		public GenClass() {
		}
	}

}
