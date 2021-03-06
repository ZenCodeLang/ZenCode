package org.openzen.zenscript.scriptingexample.tests.actual_test.packages;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class SamePackage extends ZenCodeTest {

	@Test
	public void definitionsInSamePackageAreAccessible() {
		addScript("public function doSomething() as string => 'Hello World';", "a.zs");
		addScript("println(doSomething());", "b.zs");

		executeEngine();
		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello World");
	}

	@Test
	public void definitionsInSamePackageAreAccessible_2() {
		addScript("public function doSomething() as string => 'Hello World';", "some/test/package/a.zs");
		addScript("println(doSomething());", "some/test/package/b.zs");

		executeEngine();
		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello World");
	}
}
