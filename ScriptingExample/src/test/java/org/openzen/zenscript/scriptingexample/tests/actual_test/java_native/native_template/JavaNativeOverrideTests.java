package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.native_template;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

class JavaNativeOverrideTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(Parent.class);
		requiredClasses.add(Child.class);
		return requiredClasses;
	}

	@Test
	void canOverrideMethod() {
		ScriptBuilder.create()
				.add("import test_module.Parent;")
				.add("import test_module.Child;")
				.add("val child = new Child(5);")
				.add("val result = child * 2;")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Child: 10"
		);
	}

	@ZenCodeType.Name("test_module.Parent")
	public static class Parent {
		protected final int value;

		@ZenCodeType.Constructor
		public Parent(int value) {
			this.value = value;
		}

		@ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
		public Parent mul(int value) {
			return new Parent(this.value * value);
		}

		@Override
		@ZenCodeType.Caster(implicit = true)
		public String toString() {
			return "Parent: " + value;
		}
	}

	@ZenCodeType.Name("test_module.Child")
	public static class Child extends Parent {

		@ZenCodeType.Constructor
		public Child(int value) {
			super(value);
		}

		@ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
		public Child otherMul(int value) {
			return new Child(this.value * value);
		}

		@Override
		public String toString() {
			return "Child: " + value;
		}
	}
}
