package org.openzen.zenscript.scriptingexample.tests.actual_test;

import org.junit.jupiter.api.*;
import org.openzen.zenscript.scriptingexample.tests.helpers.*;

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
}
