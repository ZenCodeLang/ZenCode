package org.openzen.zenscript.scriptingexample.tests.actual_test.generics;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

public class WildcardCastingTest extends ZenCodeTest {
	@Test
	void testPrinter() {
		ScriptBuilder.create()
				.add("import test_module.Generator;")
				.add("var generator = new Generator();")
				.add("var stuff = generator.generate();")
				.add("generator.print<string>(stuff);")
				.execute(this);
	}

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(Generator.class);
		requiredClasses.add(Singleton.class);
		return requiredClasses;
	}

	@ZenCodeType.Name("test_module.Generator")
	public static class Generator {
		@ZenCodeType.Constructor
		public Generator() {}

		@ZenCodeType.Method
		public Singleton<?> generate() {
			return new Singleton<>("Hello, World!");
		}

		@ZenCodeType.Method
		public <T> void print(Singleton<T> stuff) {
			System.out.println(stuff.value);
		}
	}

	@ZenCodeType.Name("test_module.Singleton")
	public static class Singleton<T> {
		public final T value;

		public Singleton(T value) {
			this.value = value;
		}
	}
}
