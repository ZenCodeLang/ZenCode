package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.expansions;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.ArrayList;
import java.util.List;

public class ExpandedStaticMethod extends ZenCodeTest {
	@Override
	public List<Class<?>> getRequiredClasses() {
		final ArrayList<Class<?>> classes = new ArrayList<>(super.getRequiredClasses());
		classes.add(ExpandedClass.class);
		classes.add(ExpansionUnderTest.class);
		return classes;
	}

	@Test
	public void TestThatStaticExpansionMethodWorks() {
		ScriptBuilder.create()
				.add("import test_module.Expanded;")
				.add("println(Expanded.getString());")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, ExpansionUnderTest.getString());
	}

	@ZenCodeType.Name(".Expanded")
	public static final class ExpandedClass {
	}

	@ZenCodeType.Expansion(".Expanded")
	public static final class ExpansionUnderTest {

		@ZenCodeType.ExpandedStaticMethod
		public static String getString() {
			return "Hello World";
		}
	}
}
