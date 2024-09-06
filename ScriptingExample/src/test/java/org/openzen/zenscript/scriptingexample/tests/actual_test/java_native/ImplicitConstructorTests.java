package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

class ImplicitConstructorTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(MyClass.class);
		requiredClasses.add(AnotherType.class);
		requiredClasses.add(Stringifier.class);
		return requiredClasses;
	}

	@Test
	void canBeUsed() {
		ScriptBuilder.create()
				.add("import test_module.java_native.MyClass;")
				.add("import test_module.java_native.Stringifier;")
				.add("var myClass: MyClass = 'test';")
				.add("println(Stringifier.toString(myClass));")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"test"
		);
	}


	@ZenCodeType.Name( "test_module.java_native.MyClass")
	public static class MyClass {
		private final String name;

		private MyClass(String name) {
			this.name = name;
		}

		@ZenCodeType.Constructor(implicit = true)
		public static MyClass create(String name) {
			return new MyClass(name);
		}

		@ZenCodeType.Getter("name")
		public String getName() {
			return name;
		}
	}

	@ZenCodeType.Name("test_module.java_native.AnotherType")
	public static class AnotherType {

		private final String value;

		private AnotherType(String value) {
			this.value = value;
		}

		@ZenCodeType.Constructor(implicit = true)
		public static AnotherType create(String value) {
			return new AnotherType(value);
		}
	}

	@ZenCodeType.Name("test_module.java_native.Stringifier")
	public static class Stringifier {
		@ZenCodeType.Method
		public static String toString(MyClass value) {
			return value.getName();
		}

		@ZenCodeType.Method
		public static String toString(AnotherType value) {
			return "Another type: " + value.value;
		}
	}
}
