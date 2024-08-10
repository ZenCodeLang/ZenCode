package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.expansions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

class ExpansionMemberTests extends ZenCodeTest {

	@Test
	void TestThatAnnotationWorks() {
		ScriptBuilder.create()
				.add("getExpandedClassInstance().expansionMethod();")
				.execute(this);

		Assertions.assertTrue(ExpansionUnderTest.wasCalled);
	}

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(ExpandedClass.class);
		requiredClasses.add(ExpansionUnderTest.class);
		return requiredClasses;
	}

	@SuppressWarnings("InstantiationOfUtilityClass")
	@ZenCodeType.Name(ExpandedClass.CLASS_NAME)
	public static final class ExpandedClass {

		public static final String CLASS_NAME = ".java_native.expansions.ExpandedClass";

		@ZenCodeGlobals.Global
		public static ExpandedClass getExpandedClassInstance() {
			return new ExpandedClass();
		}
	}

	@SuppressWarnings("unused")
	@ZenCodeType.Expansion(ExpandedClass.CLASS_NAME)
	public static final class ExpansionUnderTest {

		private static boolean wasCalled = false;

		public static void methodWithoutAnnotation() {
			throw new IllegalStateException("Should never be called");
		}

		@ZenCodeType.Method
		public static void expansionMethod(ExpandedClass expandedObj) {
			if (wasCalled) {
				throw new IllegalStateException("Called twice");
			}
			wasCalled = true;
		}
	}
}
