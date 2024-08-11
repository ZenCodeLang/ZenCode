package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.expansions;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.Collections;
import java.util.List;

class ExpansionMethodWithGenericArgumentsTests extends ZenCodeTest {
	@Override
	public List<String> getRequiredStdLibModules() {
		return Collections.singletonList("stdlib");
	}

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(ExpandedClass.class);
		requiredClasses.add(ExpansionUnderTest.class);
		return requiredClasses;
	}

	@Test
	void canUseStaticExpansionMethodWithGenericArguments() {
		ScriptBuilder.create()
				.add("import test_module.ExpandedClass;")
				.add("var result = ExpandedClass.parseValue<int?>('123', 'int');")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"123"
		);
	}

	@Test
	void canUseVirtualExpansionMethodWithGenericArguments() {
		ScriptBuilder.create()
				.add("import test_module.ExpandedClass;")
				.add("var result = new ExpandedClass().nameOf<int?>();")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"java.lang.Integer"
		);
	}

	@ZenCodeType.Name("test_module.ExpandedClass")
	public static class ExpandedClass {
		@ZenCodeType.Constructor
		public ExpandedClass() {
			// default .ctor
		}
	}

	@ZenCodeType.Expansion(".ExpandedClass")
	public static class ExpansionUnderTest {

		// ToDo: What signature should be used here?
		// <T> (String, String) => T
		// <T> (Class<T>, String, String) => T
		@SuppressWarnings("unchecked")
		@ZenCodeType.StaticExpansionMethod
		public static <T> T parseValue(Class<T> typeOfT, String value, String type) {
			switch (type) {
				case "int":
					return (T) Integer.valueOf(value);
				case "double":
					return (T) Double.valueOf(value);
				case "long":
					return (T) Long.valueOf(value);
				default:
					throw new IllegalArgumentException("Unknown type: " + type);
			}
		}

		@ZenCodeType.Method
		public static <T> String nameOf(ExpandedClass expandedObj, Class<T> typeOfT) {
			return typeOfT.getCanonicalName();
		}
	}
}
