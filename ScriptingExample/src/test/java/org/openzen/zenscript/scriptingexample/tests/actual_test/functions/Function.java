package org.openzen.zenscript.scriptingexample.tests.actual_test.functions;

import org.junit.jupiter.api.*;
import org.openzen.zenscript.scriptingexample.tests.helpers.*;

public class Function extends ZenCodeTest {
    
    @Test
    public void testCallSameFile() {
        ScriptBuilder.create()
                .add("public function addSomeRecipe() as void {")
                .add("    println('Hello World');")
                .add("}")
                .add("")
                .add("addSomeRecipe();")
                .execute(this);
        
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "Hello World");
    }
    
    @Test
    public void testCallDifferentFile() {
        ScriptBuilder.create()
                .add("public function addSomeRecipe() as void {")
                .add("    println('Hello World');")
                .add("}")
                .startNewScript()
                .add("addSomeRecipe();")
                .execute(this);
        
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "Hello World");
    }
}
