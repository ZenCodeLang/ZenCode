package org.openzen.zenscript.scriptingexample.tests.actual_test.packages;

import org.junit.jupiter.api.*;
import org.openzen.zenscript.scriptingexample.tests.helpers.*;

public class SamePackage extends ZenCodeTest {
    
    @Test
    public void definitionsInSamePackageAreAccessible() {
        addScript("public function doSomething() as string => 'Hello World';", "a.zs");
        addScript("println(doSomething());", "b.zs");
        
        executeEngine();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "Hello World");
    }
    
    @Test
    public void definitionsInSamePackageAreAccessible_2() {
        addScript("public function doSomething() as string => 'Hello World';", "some/test/package/a.zs");
        addScript("println(doSomething());", "some/test/package/b.zs");
    
        executeEngine();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "Hello World");
    }
}
