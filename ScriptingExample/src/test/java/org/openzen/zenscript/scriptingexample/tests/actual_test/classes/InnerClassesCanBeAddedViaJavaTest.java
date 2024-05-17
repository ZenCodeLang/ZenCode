package org.openzen.zenscript.scriptingexample.tests.actual_test.classes;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;

/**
 * Test scenario to check that inner classes can be added
 * <br/>
 * Reason for test:
 * <a href="https://github.com/CraftTweaker/CraftTweaker/issues/1727">Link to GitHub issue</a>
 */
public class InnerClassesCanBeAddedViaJavaTest extends ZenCodeTest {

	@Test
	public void testInnerClassesCanBeAddedViaJava_Outer() {
		ScriptBuilder.create()
				.add("import test_module.Outer;")
				.add("println(Outer.getName());")
				.execute(this);

		this.logger.assertPrintOutputSize(1);
		this.logger.assertPrintOutput(0, "Outer");
	}

	@Test
	public void testInnerClassesCanBeAddedViaJava_Inner() {
		ScriptBuilder.create()
				.add("import test_module.Outer;")
				.add("println(Outer.Inner.getName());")
				.execute(this);

		this.logger.assertPrintOutputSize(1);
		this.logger.assertPrintOutput(0, "Inner");
	}

	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(Outer.class);
		return requiredClasses;
	}


	@ZenCodeType.Name("test_module.Outer")
	public static class Outer {
		@ZenCodeType.Method
		public static String getName() {
			return "Outer";
		}

		@ZenCodeType.Inner
		public static class Inner {
			@ZenCodeType.Method
			public static String getName() {
				return "Inner";
			}
		}
	}
}
