package org.openzen.zenscript.scriptingexample.tests.actual_test.arithmethic_operators;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.openzen.zenscript.scriptingexample.tests.helpers.*;

public class AdditionTest extends ZenCodeTest {
    
    @ParameterizedTest(name = "{0} + {1}")
    @CsvSource({"0,0", "0,1", "10,10", "-1,1", "815,4711"})
    public void testSomeAdditions(int i, int j) {
        addScript(String.format("println(%d + %d);", i, j));
        executeEngine();
        
        logger.assertNoErrors();
        logger.assertNoWarnings();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, String.valueOf(i + j));
    }
    
    @Test
    public void testChainedAdditions() {
        addScript("println(1+2+3+4+5+6+7+8+9+10);");
        executeEngine();
    
        logger.assertNoErrors();
        logger.assertNoWarnings();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, String.valueOf(55));
    }
}
