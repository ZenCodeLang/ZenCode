package org.openzen.zenscript.scriptingexample.tests.actual_test.arrays;

import org.junit.jupiter.api.*;
import org.openzen.zenscript.scriptingexample.tests.helpers.*;

public class ArrayOperators extends ZenCodeTest {
    @Test
    public void containsReturnsTrueForMatch() {
        ScriptBuilder.create()
                .add("var array = ['a', 'b', 'c'] as string[];")
                .add("println(('a' in array) as string);")
                .execute(this);
        
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "true");
    }
    
    @Test
    public void containsReturnsFalseForNonMatch() {
        ScriptBuilder.create()
                .add("var array = ['a', 'b', 'c'] as string[];")
                .add("println(('d' in array) as string);")
                .execute(this);
        
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "false");
    }
}
