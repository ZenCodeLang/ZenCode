package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.expansions;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

class ExpansionMethodWithGenericArgumentsTests extends ZenCodeTest {
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

	@ZenCodeType.Name("test_module.ExpandedClass")
	public static class ExpandedClass {
	}

	@ZenCodeType.Expansion("test_module.ExpandedClass")
	public static class ExpansionUnderTest {

		// ToDo: What signature should be used here?
		// <T> (String, String) => T
		// <T> (Class<T>, String, String) => T
		@SuppressWarnings("unchecked")
		@ZenCodeType.StaticExpansionMethod
		public static <T> T parseValue(String value, String type) {
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
	}
}
