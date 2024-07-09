package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CanUseJavaUtilTests extends ZenCodeTest {

	@Override
	public List<String> getRequiredStdLibModules() {
		return Collections.singletonList("stdlib");
	}

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> classes = super.getRequiredClasses();
		classes.add(Aggregator.class);
		return classes;
	}

	@Test
	void canUseAggregate() {
		ScriptBuilder.create()
				.add("import test_module.java_native.Aggregator;")
				.add("for i in Aggregator.aggregate('Hello', 'World') {")
				.add("  println(i);")
				.add("}")
				.execute(this);

		logger.assertNoErrors();
		logger.printlnOutputs().assertLinesInOrder(
				"Hello",
				"World"
		);
	}

	@ZenCodeType.Name("test_module.java_native.Aggregator")
	public static class Aggregator {

		@ZenCodeType.Method
		public static List<String> aggregate(String a, String b) {
			return Arrays.asList(a, b);
		}
	}
}
