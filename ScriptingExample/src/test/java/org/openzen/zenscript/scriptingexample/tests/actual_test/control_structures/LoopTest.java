package org.openzen.zenscript.scriptingexample.tests.actual_test.control_structures;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class LoopTest extends ZenCodeTest {

	@Test
	public void validateCast() {
		addScript("var a = 1; println(a); println(a * 2);");
		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "1");
		logger.assertPrintOutput(1, "2");
	}

	@Test
	public void validateOutput() {
		addScript("for i in 0 .. 100 println(i as int);");
		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(100);
		for (int i = 0; i < 100; i++) {
			logger.assertPrintOutput(i, String.valueOf(i));
		}
	}


	@Test
	public void canReuseLoopVariables() {
		ScriptBuilder.create()
				.add("var items = [1, 2, 3];")
				.add("println('before 1st loop');")
				.add("")
				.add("for i in items {")
				.add("    println('Hello');")
				.add("}")
				.add("")
				.add("println('between loops');")
				.add("")
				.add("for i in items {")
				.add("    break;")
				.add("}")
				.add("println('done');")
				.execute(this);

		logger.assertPrintOutputSize(6);
		logger.assertPrintOutput(0, "before 1st loop");
		logger.assertPrintOutput(1, "Hello");
		logger.assertPrintOutput(2, "Hello");
		logger.assertPrintOutput(3, "Hello");
		logger.assertPrintOutput(4, "between loops");
		logger.assertPrintOutput(5, "done");

	}
}
