package org.openzen.zenscript.scriptingexample.tests.actual_test.expansions;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class GenericExpansions extends ZenCodeTest {

	@Test
	public void TestThatSameTypeParameterWorks() {
		ScriptBuilder.create()
				.add("public class MyClass<T> {")
				.add("    public this() {}")
				.add("}")
				.add("")
				.add("public expand MyClass<string> {")
				.add("    public printMe() as void {println(\"Hello World\");}")
				.add("}")
				.add("")
				.add("new MyClass<string>().printMe();")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello World");
	}

	@Test
	public void TestThatDifferentTypeParameterWorks() {
		ScriptBuilder.create()
				.add("public class MyClass<T> {")
				.add("    public this() {}")
				.add("}")
				.add("")
				.add("public expand MyClass<string> {")
				.add("    public printMe() as void {println(\"Hello World\");}")
				.add("}")
				.add("")
				.add("new MyClass<bool>().printMe();")
				.execute(this, ScriptBuilder.LogTolerance.ALLOW_ERRORS);

		logger.assertPrintOutputSize(0);
		logger.errors().assertSize(1);
		logger.errors().assertLineContains(0, "No such member: printMe");
	}
}
