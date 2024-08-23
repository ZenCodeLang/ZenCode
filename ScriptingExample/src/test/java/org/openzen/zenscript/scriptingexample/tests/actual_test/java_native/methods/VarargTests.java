package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.methods;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

class VarargTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(ClassWithVararg.class);
		return requiredClasses;
	}

	@Test
	void canCallVarArgWithNoArguments() {
		ScriptBuilder.create()
				.add("import test_module.java_native.methods.ClassWithVararg;")
				.add("var result = ClassWithVararg.join();")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"[]"
		);
	}

	@Test
	void canCallVarArgWithOneArgument() {
		ScriptBuilder.create()
				.add("import test_module.java_native.methods.ClassWithVararg;")
				.add("var result = ClassWithVararg.join('one');")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"[one]"
		);
	}

	@Test
	void canCallVarArgWithManyArguments() {
		ScriptBuilder.create()
				.add("import test_module.java_native.methods.ClassWithVararg;")
				.add("var result = ClassWithVararg.join('one', 'two', 'three');")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"[one, two, three]"
		);
	}

	@Test
	void canCallVarArgWithArray() {
		ScriptBuilder.create()
				.add("import test_module.java_native.methods.ClassWithVararg;")
				.add("var result = ClassWithVararg.join(['one', 'two', 'three']);")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"[one, two, three]"
		);
	}

	@ZenCodeType.Name("test_module.java_native.methods.ClassWithVararg")
	public static final class ClassWithVararg {

		@ZenCodeType.Method
		public static String join(String... values) {
			return '[' + String.join(", ", values) + ']';
		}
	}
}
