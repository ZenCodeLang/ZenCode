package org.openzen.zenscript.scriptingexample.tests.actual_test.joined_tests;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

public class TypeInferenceBlockingTest extends ZenCodeTest {

	@Test
	public void doTheTest() {
		ScriptBuilder.create()
				.add("public class MyClass {")
				.add("		public this(thing as test_module.MyEnum) {")
				.add("		println(thing.name);")
				.add("	}")
				.add("}")
				.add("new MyClass(test_module.MyEnum.A);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "A");
	}


	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(MyEnum.class);
		return requiredClasses;
	}

	@ZenCodeType.Name("test_module.MyEnum")
	public enum MyEnum {
		A, B, C
	}
}
