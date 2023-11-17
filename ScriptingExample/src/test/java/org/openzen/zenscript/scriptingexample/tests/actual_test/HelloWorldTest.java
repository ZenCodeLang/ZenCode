package org.openzen.zenscript.scriptingexample.tests.actual_test;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class HelloWorldTest extends ZenCodeTest {

	@Test
	public void helloWorld() {
		addScript("println('hello world');");
		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "hello world");
	}

	@Test
	public void helloVariables() {
		addScript("var x = 'hello'; println(x);");
		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "hello");
	}

	@Test
	public void helloVariables2() {
		addScript("var x = 'hello'; println(x + ' world');");
		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "hello world");
	}

	@Test
	public void helloVariables3() {
		addScript("var x = 'hello'; x += ' world'; println(x);");
		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "hello world");
	}
}
