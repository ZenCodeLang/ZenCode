package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.operators;

import org.junit.jupiter.api.*;
import org.openzen.zencode.java.*;
import org.openzen.zenscript.scriptingexample.tests.helpers.*;

import java.util.*;

public class OperatorEquals extends ZenCodeTest {
    
    @Override
    public List<Class<?>> getRequiredClasses() {
        final List<Class<?>> requiredClasses = super.getRequiredClasses();
        requiredClasses.add(ClassWithEqualsOperator.class);
        requiredClasses.add(ClassWithEqualsAndNotEqualsOperator.class);
        return requiredClasses;
    }
    
    @Test
    public void canUseEqualsOperator() {
        ScriptBuilder.create()
                .add("var a = createWithEquals('A');")
                .add("var b = createWithEquals('B');")
                .add("var a2 = createWithEquals('A');")
                .add("println(a == b);")
                .add("println(a == a2);")
                .execute(this);
        
        logger.assertPrintOutputSize(2);
        logger.assertPrintOutput(0, "false");
        logger.assertPrintOutput(1, "true");
    }
    
    @Test
    public void canUseImplicitNotEqualsOperator() {
        ScriptBuilder.create()
                .add("var a = createWithEquals('A');")
                .add("var b = createWithEquals('B');")
                .add("var a2 = createWithEquals('A');")
                .add("println(a != b);")
                .add("println(a != a2);")
                .execute(this);
        
        logger.assertPrintOutputSize(2);
        logger.assertPrintOutput(0, "true");
        logger.assertPrintOutput(1, "false");
    }
    
    @Test
    public void useExplicitNotEqualsWhenPresent() {
        ClassWithEqualsAndNotEqualsOperator.logger = logger;
        ScriptBuilder.create()
                .add("var a = createWithEqualsAndNotEquals('A');")
                .add("var b = createWithEqualsAndNotEquals('B');")
                .add("var a2 = createWithEqualsAndNotEquals('A');")
                .add("println(a != b);")
                .add("println(a != a2);")
                .add("println(a == b);")
                .add("println(a == a2);")
                .execute(this);
    
        logger.assertPrintOutputSize(8);
        logger.assertPrintOutput(0, "notEqualsCalled");
        logger.assertPrintOutput(1, "true");
        logger.assertPrintOutput(2, "notEqualsCalled");
        logger.assertPrintOutput(3, "false");
        logger.assertPrintOutput(4, "equalsCalled");
        logger.assertPrintOutput(5, "false");
        logger.assertPrintOutput(6, "equalsCalled");
        logger.assertPrintOutput(7, "true");
    }
    
    
    @ZenCodeType.Name("test_module.java_native.operator.equals.ClassWithEqualsOperator")
    public static final class ClassWithEqualsOperator {
    
        public ClassWithEqualsOperator(String value) {
            this.value = value;
        }
    
        @ZenCodeGlobals.Global
        public static ClassWithEqualsOperator createWithEquals(String value) {
            return new ClassWithEqualsOperator(value);
        }
        
        private final String value;
    
        @ZenCodeType.Operator(ZenCodeType.OperatorType.EQUALS)
        public boolean equalsOperator(ClassWithEqualsOperator other) {
            return this.value.equals(other.value);
        }
    }
    
    @ZenCodeType.Name("test_module.java_native.operator.equals.ClassWithEqualsAndNotEqualsOperator")
    public static final class ClassWithEqualsAndNotEqualsOperator {
        private static ZenCodeTestLogger logger;
        
        public ClassWithEqualsAndNotEqualsOperator(String value) {
            this.value = value;
        }
        
        @ZenCodeGlobals.Global
        public static ClassWithEqualsAndNotEqualsOperator createWithEqualsAndNotEquals(String value) {
            return new ClassWithEqualsAndNotEqualsOperator(value);
        }
        
        private final String value;
        
        @ZenCodeType.Operator(ZenCodeType.OperatorType.EQUALS)
        public boolean equalsOperator(ClassWithEqualsAndNotEqualsOperator other) {
            logger.logPrintln("equalsCalled");
            return this.value.equals(other.value);
        }
    
        @ZenCodeType.Operator(ZenCodeType.OperatorType.NOTEQUALS)
        public boolean notEqualsOperator(ClassWithEqualsAndNotEqualsOperator other) {
            logger.logPrintln("notEqualsCalled");
            return !this.value.equals(other.value);
        }
    }
}
