package org.openzen.zenscript.scriptingexample.tests.actual_test.expansions;

import org.junit.jupiter.api.*;
import org.openzen.zenscript.scriptingexample.tests.helpers.*;

public class StringExpansions extends ZenCodeTest {
    @Test
    public void toUpperCaseMethod() {
        ScriptBuilder.create()
                .add("println('hello world'.toUpperCase());")
                .execute(this);
    
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "Hello World".toUpperCase());
    }
}
