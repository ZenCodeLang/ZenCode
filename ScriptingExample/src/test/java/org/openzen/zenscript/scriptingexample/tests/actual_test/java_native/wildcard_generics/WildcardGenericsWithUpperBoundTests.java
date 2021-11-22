package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.wildcard_generics;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

public class WildcardGenericsWithUpperBoundTests extends ZenCodeTest {


	@Test
	public void wildcardsWithUpperLayerWork() {
		ScriptBuilder.create()
				.add("var testClass = getTestClass();")
				.add("var value = testClass.getValue();")
				.add("println(value);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, MyGlobalClass.getTestClass().getValue());
	}


	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(MyTestClass.class);
		requiredClasses.add(MyGlobalClass.class);
		return requiredClasses;
	}

	@ZenCodeType.Name("test_module.MyGlobalClass")
	public static final class MyGlobalClass {
		@ZenCodeGlobals.Global
		public static MyTestClass<? extends String> getTestClass() {
			return new MyTestClass<>("Hello World");
		}
	}

	@ZenCodeType.Name("test_module.MyTestClass")
	public static final class MyTestClass<T> {
		
		public final T value;

		public MyTestClass(T value) {
			this.value = value;
		}

		@ZenCodeType.Method
		public T getValue() {
			return value;
		}
	}
}
