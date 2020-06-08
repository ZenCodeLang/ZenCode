package org.openzen.zenscript.scriptingexample.tests.actual_test.classes;

import org.junit.jupiter.api.*;
import org.openzen.zenscript.scriptingexample.tests.helpers.*;

public class AddingClasses extends ZenCodeTest {
    
    @Test
    public void emptyClassCompiles() {
        ScriptBuilder.create().add("public class SomeClass {").add("}").execute(this);
        logger.assertNoErrors();
    }
    
    @Test
    public void memberGettable() {
        ScriptBuilder.create()
                .add("public class SomeClass {")
                .add("    public var x as string;")
                .add("    public this(){x = 'Hello World';}")
                .add("}")
                .add("")
                .add("println(new SomeClass().x);")
                .execute(this);
        
        logger.assertNoErrors();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "Hello World");
    }
    
    @Test
    public void functionCallable() {
        ScriptBuilder.create()
                .add("public class SomeClass {")
                .add("    public this(){}")
                .add("    public callMeMaybe() as void {println('Hello World!');}")
                .add("}")
                .add("")
                .add("new SomeClass().callMeMaybe();")
                .execute(this);
        
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "Hello World!");
    }
    
    @Test
    public void classAccessibleFromOtherScript() {
        ScriptBuilder.create()
                .add("public class SomeClass {")
                .add("    public this(){}")
                .add("    public callMeMaybe() as void {println('Hello World!');}")
                .add("}")
                .add("")
                .startNewScript()
                .add("new SomeClass().callMeMaybe();")
                .execute(this);
        
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "Hello World!");
    }
    
    @Test
    public void fieldInitializerSetsField() {
        ScriptBuilder.create()
                .add("public class SomeClass {")
                .add("    public var x as string = 'Hello World';")
                .add("    public this(){}")
                .add("}")
                .add("")
                .add("println(new SomeClass().x);")
                .execute(this);
        
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "Hello World");
    }
    
    @Test
    public void constructorOverridesFieldInitializer() {
        ScriptBuilder.create()
                .add("public class SomeClass {")
                .add("    public var x as string = 'Hello World';")
                .add("    public this(){this.x = 'Goodbye World';}")
                .add("}")
                .add("")
                .add("println(new SomeClass().x);")
                .execute(this);
        
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "Goodbye World");
    }
}
