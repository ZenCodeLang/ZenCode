package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.native_template;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

public class JavaNativeConstructorTest extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> classes = super.getRequiredClasses();
		classes.add(ClassWithImplicitConstructor.class);
		classes.add(ClassWithOverloadedConstructor.class);
		return classes;
	}

	@Test
	void canCallImplicitConstructor() {
		ScriptBuilder.create()
				.add("import test_module.ClassWithImplicitConstructor;")
				.add("var child = new ClassWithImplicitConstructor();")
				.add("println(child.greetMe());")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Hi from class with implicit constructor"
		);
	}

	@Test
	void canMatchOverloadedConstructor() {
		ScriptBuilder.create()
				.add("import test_module.ClassWithOverloadedConstructor;")
				.add("var child = new ClassWithOverloadedConstructor(1);")
				.add("println(child.value);")
				.add("var child2 = new ClassWithOverloadedConstructor('test');")
				.add("println(child2.value);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Int value: 1",
				"String value: test"
		);
	}

	@ZenCodeType.Name("test_module.ClassWithImplicitConstructor")
	public static class ClassWithImplicitConstructor {
		@ZenCodeType.Method
		public String greetMe() {
			return "Hi from class with implicit constructor";
		}
	}

	@ZenCodeType.Name("test_module.ClassWithOverloadedConstructor")
	public static class ClassWithOverloadedConstructor {
		private final String value;

		@ZenCodeType.Constructor
		public ClassWithOverloadedConstructor(int value) {
			this.value = "Int value: " + value;
		}

		@ZenCodeType.Constructor
		public ClassWithOverloadedConstructor(String value) {
			this.value = "String value: " + value;
		}

		@ZenCodeType.Getter("value")
		public String getValue() {
			return value;
		}
	}
}
