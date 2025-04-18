package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class JavaObjectTests extends ZenCodeTest {

	@Override
	public List<String> getRequiredStdLibModules() {
		return Collections.singletonList("stdlib");
	}

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> classes = super.getRequiredClasses();
		classes.add(Stringer.class);
		return classes;
	}

	@Test
	void canCastToObjectToString() {
		ScriptBuilder.create()
				.add("import test_module.java_native.Stringer;")
				.add("println(Stringer.toString('Hello World'));")
				.execute(this);

		logger.assertNoErrors();
		logger.printlnOutputs().assertLinesInOrder("Hello World");
	}

	@Test
	void canUseWildcardGenerics() {
		ScriptBuilder.create()
				.add("import test_module.java_native.Stringer;")
				.add("import stdlib.List;")
				.add("val value as List<string> = ['Hello', 'World'];")
				.add("println(Stringer.toJoinedString(value));")
				.execute(this);

		logger.assertNoErrors();
		logger.printlnOutputs().assertLinesInOrder("Hello, World");
	}

	@ZenCodeType.Name("test_module.java_native.Stringer")
	public static class Stringer {

		@ZenCodeType.Method
		public static String toString(Object value) {
			return value.toString();
		}

		@ZenCodeType.Method
		public static String toJoinedString(List<?> values) {
			return String.join(", ", values.stream().map(Object::toString).toArray(String[]::new));
		}
	}
}