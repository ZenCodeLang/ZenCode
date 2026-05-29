package org.openzen.zenscript.scriptingexample.tests.actual_test.lambda;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class LambdaTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(LambdaTests.MyClass.class);
		requiredClasses.add(LambdaTests.SequenceContext.class);
		requiredClasses.add(LambdaTests.SequenceBuilder.class);
		requiredClasses.add(Globals.class);
		return requiredClasses;
	}

	@Test
	void testArrayOverloads() {
		ScriptBuilder.create()
				.add("sequence().sleepUntil((str) => { if 1 == 1 {return true;} return str.raining();});")
				.execute(this);

		logger.assertPrintOutputSize(0);
	}

	@ZenCodeType.Name("test_module.MyClass")
	public static final class MyClass {

		@ZenCodeType.Method
		public boolean raining() {
			return true;
		}

	}

	@ZenCodeType.Name("test_module.Globals")
	public static final class Globals {

		@ZenCodeGlobals.Global
		public static SequenceBuilder<MyClass, String> sequence() {
			return new SequenceBuilder<>();
		}
	}

	@ZenCodeType.Name("test_module.SequenceBuilder")
	public static class SequenceBuilder<T, U> {

		@ZenCodeType.Method
		public SequenceBuilder<T, U> sleepUntil(Predicate<T> condition) {

			return this;
		}

		@ZenCodeType.Method
		public SequenceBuilder<T, U> sleepUntil(BiPredicate<T, SequenceContext<T, U>> condition) {

			return this;
		}

	}

	@ZenCodeType.Name("test_module.SequenceContext")
	public static class SequenceContext<T, U> {

	}
}
