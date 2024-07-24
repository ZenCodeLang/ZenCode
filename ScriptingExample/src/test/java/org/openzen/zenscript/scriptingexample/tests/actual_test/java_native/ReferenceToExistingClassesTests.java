package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReferenceToExistingClassesTests extends ZenCodeTest {

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(MyExposedClass.class);
		return requiredClasses;
	}

	@Test
	public void canCallConstructor() {
		ScriptBuilder.create()
				.add("import test_module.some.subpackage.MyExposedClass;")
				.add("var myExposedClass = new MyExposedClass('test');")
				.add("println(myExposedClass.name);")
				.execute(this);

		logger.assertNoErrors();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "test");
	}

	@Test
	public void canCallStaticMethod() {
		ScriptBuilder.create()
				.add("import test_module.some.subpackage.MyExposedClass;")
				.add("var repeated = MyExposedClass.repeat('test', 3);")
				.add("println(repeated);")
				.execute(this);

		logger.assertNoErrors();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "testtesttest");
	}


	@ZenCodeType.Name("test_module.some.subpackage.MyExposedClass")
	public static final class MyExposedClass {

		private final String name;

		@ZenCodeType.Constructor
		public MyExposedClass(String name) {
			this.name = name;
		}

		@ZenCodeType.Getter("name")
		public String getName() {
			return name;
		}

		@ZenCodeType.Method
		public static String repeat(String str, int times) {
			return IntStream.range(0, times)
					.mapToObj(i -> str)
					.collect(Collectors.joining());
		}
	}
}
