package org.openzen.zenscript.scriptingexample.tests.actual_test.classes;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class SuperConstructor extends ZenCodeTest {

	@Test
	public void implicitlyCallSuperConstructor_Possible() {
		ScriptBuilder.create()
				.add("public abstract virtual class MySuperClass {")
				.add("    public abstract print() as void;")
				.add("}")
				.add("")
				.add("public class MyChildClass : MySuperClass {")
				.add("    public print() as void {")
				.add("        println('Hello from MyChildClass');")
				.add("    }")
				.add("}")
				.add("")
				.add("new MyChildClass().print();")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello from MyChildClass");
	}

	@Test
	public void implicitlyCallSuperConstructor_NOT_Possible() {
		ScriptBuilder.create()
				.add("public abstract virtual class MySuperClass {")
				.add("    public this(someInput as string){}")
				.add("    public abstract print() as void;")
				.add("}")
				.add("")
				.add("public class MyChildClass : MySuperClass {")
				.add("    public print() as void {")
				.add("        println('Hello from MyChildClass');")
				.add("    }")
				.add("}")
				.add("")
				.add("new MyChildClass().print();")
				.execute(this, ScriptBuilder.LogTolerance.ALLOW_ERRORS);

		logger.errors().assertSize(1);
		logger.errors().assertLine(0, "ERROR test_script_1.zs:6:0: Constructor not forwarded to base type");
	}

	@Test
	public void explicitlyCallSuperConstructor() {
		ScriptBuilder.create()
				.add("public abstract virtual class MySuperClass {")
				.add("    public this(someInput as string){}")
				.add("    public abstract print() as void;")
				.add("}")
				.add("")
				.add("public class MyChildClass : MySuperClass {")
				.add("    public this() => super('');")
				.add("    public print() as void {")
				.add("        println('Hello from MyChildClass');")
				.add("    }")
				.add("}")
				.add("")
				.add("new MyChildClass().print();")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello from MyChildClass");
	}
}
