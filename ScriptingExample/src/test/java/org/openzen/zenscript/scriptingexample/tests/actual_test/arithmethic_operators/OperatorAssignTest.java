package org.openzen.zenscript.scriptingexample.tests.actual_test.arithmethic_operators;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class OperatorAssignTest extends ZenCodeTest {
    
    @Test
    public void testAddAssign() {
        
        ScriptBuilder.create()
                .add("var x = 10;")
                .add("x += 5;")
                .add("println(x);")
                .execute(this);
        
        logger.assertNoErrors();
        logger.assertNoWarnings();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, String.valueOf(15));
    }
    
    @Test
    public void testSubAssign() {
        
        ScriptBuilder.create()
                .add("var x = 10;")
                .add("x -= 5;")
                .add("println(x);")
                .execute(this);
        
        logger.assertNoErrors();
        logger.assertNoWarnings();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, String.valueOf(5));
    }
    
    @Test
    public void testCatAssign() {
        // ToDo: Have a builtin with the CAT (x ~ y) binary operator?
        ScriptBuilder.create()
                .add("public class CatTest {")
                .add("  public var value as string : get, set;")
                .add("  public this(value as string) {")
                .add("      this.value = value;")
                .add("  }")
                .add("  public ~(value as string) as CatTest {")
                .add("      this.value += value;")
                .add("      return this;")
                .add("  }")
                .add("}")
                .add("var x = new CatTest('Hello');")
                .add("x ~ ' World!';")
                .add("println(x.value);")
                .execute(this);
        
        logger.assertNoErrors();
        logger.assertNoWarnings();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "Hello World!");
    }
    
    @Test
    public void testMulAssign() {
        
        ScriptBuilder.create()
                .add("var x = 10;")
                .add("x *= 5;")
                .add("println(x);")
                .execute(this);
        
        logger.assertNoErrors();
        logger.assertNoWarnings();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, String.valueOf(50));
    }
    
    @Test
    public void testDivAssign() {
        
        ScriptBuilder.create()
                .add("var x = 50;")
                .add("x /= 10;")
                .add("println(x);")
                .execute(this);
        
        logger.assertNoErrors();
        logger.assertNoWarnings();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, String.valueOf(5));
    }
    
    @Test
    public void testModAssign() {
        
        ScriptBuilder.create()
                .add("var x = 10;")
                .add("x %= 3;")
                .add("println(x);")
                .execute(this);
        
        logger.assertNoErrors();
        logger.assertNoWarnings();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, String.valueOf(1));
    }
    
    @Test
    public void testOrAssign() {
        
        ScriptBuilder.create()
                .add("var x = 0b0001;")
                .add("x |= 0b0010;")
                .add("println(x);")
                .execute(this);
        
        logger.assertNoErrors();
        logger.assertNoWarnings();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, String.valueOf(0b0011));
    }
    
    @Test
    public void testAndAssign() {
        
        ScriptBuilder.create()
                .add("var x = 0b1001;")
                .add("x &= 0b0011;")
                .add("println(x);")
                .execute(this);
        
        logger.assertNoErrors();
        logger.assertNoWarnings();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, String.valueOf(0b0001));
    }
    
    @Test
    public void testXORAssign() {
        
        ScriptBuilder.create()
                .add("var x = 0b001;")
                .add("x ^= 0b0011;")
                .add("println(x);")
                .execute(this);
        
        logger.assertNoErrors();
        logger.assertNoWarnings();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, String.valueOf(0b0010));
    }
    
    @Test
    public void testSHLAssign() {
        
        ScriptBuilder.create()
                .add("var x = 4;")
                .add("x <<= 1;")
                .add("println(x);")
                .execute(this);
        
        logger.assertNoErrors();
        logger.assertNoWarnings();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "8");
    }
    
    @Test
	@Disabled("No type has the USHR operator atm!")
    public void testUSHRAssign() {

        ScriptBuilder.create()
				.add("var x = -4;")
				.add("x >>>= 1;")
				.add("println(x);")
				.execute(this);
        
        logger.assertNoErrors();
        logger.assertNoWarnings();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, String.valueOf(-4 >>> 1));
    }
    
    @Test
    public void testSHRAssign() {

		ScriptBuilder.create()
				.add("var x = -4;")
				.add("x >>= 1;")
				.add("println(x);")
				.execute(this);
        
        logger.assertNoErrors();
        logger.assertNoWarnings();
        logger.assertPrintOutputSize(1);
        logger.assertPrintOutput(0, "-2");
    }
    
    
}
