package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.expansions;

import org.junit.jupiter.api.*;
import org.openzen.zencode.java.*;
import org.openzen.zenscript.scriptingexample.tests.helpers.*;

import java.util.*;

public class ExpansionMembers extends ZenCodeTest {
    
    @Test
    public void TestThatAnnotationWorks() {
        ScriptBuilder.create()
                .add("getExpandedClassInstance().expansionMethod();")
                .execute(this);
        
        Assertions.assertTrue(ExpansionUnderTest.wasCalled);
    }
    
    @Override
    public List<Class<?>> getRequiredClasses() {
        final List<Class<?>> requiredClasses = super.getRequiredClasses();
        requiredClasses.add(ExpandedClass.class);
        requiredClasses.add(ExpansionUnderTest.class);
        return requiredClasses;
    }
    
    @SuppressWarnings("InstantiationOfUtilityClass")
    @ZenCodeType.Name(ExpandedClass.className)
    public static final class ExpandedClass {
        
        public static final String className = ".java_native.expansions.ExpandedClass";
        
        @ZenCodeGlobals.Global
        public static ExpandedClass getExpandedClassInstance() {
            return new ExpandedClass();
        }
    }
    
    @SuppressWarnings("unused")
    @ZenCodeType.Expansion(ExpandedClass.className)
    public static final class ExpansionUnderTest {
        
        private static boolean wasCalled = false;
        
        public static void methodWithoutAnnotation() {
        }
        
        @ZenCodeType.Method
        public static void expansionMethod(ExpandedClass _this) {
            if(wasCalled) {
                throw new IllegalStateException("Called twice");
            }
            wasCalled = true;
        }
    }
}
