package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

public class RawBoundedTypeInHeaderTest extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(Parent.class);
		requiredClasses.add(Child.class);
		requiredClasses.add(BoundedGenClass.class);
		requiredClasses.add(TestGlobals.class);
		return requiredClasses;
	}

	@Test
	void rawBoundedTypeAcceptsParameterizedValue() {
		addScript("acceptRaw(new test_module.java_native.BoundedGenClass<test_module.java_native.Child>(new test_module.java_native.Child()));");

		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
	}

	@Test
	void rawBoundedTypeExposesBoundMembers() {
		addScript("println(makeRaw().unwrap().greet());");

		executeEngine();

		logger.assertNoErrors();
		logger.printlnOutputs().assertLinesInOrder("Hi from child");
	}

	@ZenCodeType.Name("test_module.java_native.RawBoundedTestGlobals")
	public static final class TestGlobals {
		@ZenCodeGlobals.Global
		public static void acceptRaw(BoundedGenClass clazz) {
		}

		@ZenCodeGlobals.Global
		public static BoundedGenClass makeRaw() {
			return new BoundedGenClass<>(new Child());
		}
	}

	@ZenCodeType.Name("test_module.java_native.Parent")
	public static class Parent {
		@ZenCodeType.Method
		public String greet() {
			return "Hi from parent";
		}
	}

	@ZenCodeType.Name("test_module.java_native.Child")
	public static class Child extends Parent {
		@ZenCodeType.Constructor
		public Child() {
		}

		@Override
		@ZenCodeType.Method
		public String greet() {
			return "Hi from child";
		}
	}

	@ZenCodeType.Name("test_module.java_native.BoundedGenClass")
	public static final class BoundedGenClass<T extends Parent> {
		private final T value;

		@ZenCodeType.Constructor
		public BoundedGenClass(T value) {
			this.value = value;
		}

		@ZenCodeType.Method
		public T unwrap() {
			return value;
		}
	}
}
