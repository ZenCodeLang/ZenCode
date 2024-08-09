package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native;


import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

public class UsingGenericTypeParameters extends ZenCodeTest {
	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> classes = super.getRequiredClasses();
		classes.add(MyList.class);
		classes.add(Usage.class);
		classes.add(ExtendedList.class);
		classes.add(MyListExpansion.class);
		return classes;
	}

	@Test
	void canUseGenericTypeParameters1() {
		ScriptBuilder.create()
				.add("import test_module.java_native.Usage;")
				.add("println(Usage.test()[0]);")
				.execute(this);

		logger.assertNoErrors();
		logger.printlnOutputs().assertLinesInOrder("Hello");
	}

	@Test
	void canUseGenericTypeParameters2() {
		ScriptBuilder.create()
				.add("import test_module.java_native.ExtendedList;")
				.add("println(new ExtendedList<string>(['Hello', 'World'])[0]);")
				.execute(this);

		logger.assertNoErrors();
		logger.printlnOutputs().assertLinesInOrder("Hello");
	}

	/*@Test
	void canUseGenericTypeParameters3() {
		ScriptBuilder.create()
				.add("import test_module.java_native.ExtendedList;")
				.add("println(ExtendedList<string>.create(['Hello', 'World'])[0]);")
				.execute(this);

		logger.assertNoErrors();
		logger.printlnOutputs().assertLinesInOrder("Hello");
	}*/

	@Test
	void canMakeGenericExpansion() {
		ScriptBuilder.create()
				.add("import test_module.java_native.Usage;")
				.add("println(Usage.test().element(0));")
				.execute(this);

		logger.assertNoErrors();
		logger.printlnOutputs().assertLinesInOrder("Hello");
	}

	@ZenCodeType.Name("test_module.java_native.MyList")
	public static class MyList<T> {
		private T[] elements;

		@ZenCodeType.Constructor
		public MyList(T[] elements) {
			this.elements = elements;
		}

		@ZenCodeType.Operator(ZenCodeType.OperatorType.INDEXGET)
		public T element(int index) {
			return elements[index];
		}
	}

	@ZenCodeType.Name("test_module.java_native.Usage")
	public static class Usage {
		@ZenCodeType.Method
		public static MyList<String> test() {
			return new MyList<>(new String[] {"Hello", "World"});
		}
	}

	@ZenCodeType.Name("test_module.java_native.ExtendedList")
	public static class ExtendedList<T> extends MyList<T> {
		/*@ZenCodeType.Method
		public static <T> ExtendedList<T> create(T[] elements) {
			return new ExtendedList<>(elements);
		}*/

		@ZenCodeType.Constructor
		public ExtendedList(T[] elements) {
			super(elements);
		}
	}

	@ZenCodeType.Expansion(".java_native.MyList<string>")
	public static class MyListExpansion {
		@ZenCodeType.Method
		public static String element(MyList<String> list, int index) {
			return list.element(index);
		}
	}
}
