package org.openzen.zenscript.scriptingexample.tests.actual_test.classes;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

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
				.add("    public this(){this.x = 'Hello World';}") // ToDo: Does ZC mandate `this.` prefix?
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

	@Test
	public void staticMethodAccessibleWithoutInstance() {
		ScriptBuilder.create()
				.add("public class SomeClass {")
				.add("    public static someMethod() as string => 'Hello World';")
				.add("}")
				.add("println(SomeClass.someMethod());")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello World");
	}

	@Test
	public void staticFieldCallable() {
		ScriptBuilder.create()
				.add("public class TestClass {")
				.add("public static val test as string = 'Hello World';")
				.add("}")
				.add("")
				.startNewScript()
				.add("println(TestClass.test);")
				.execute(this);

		logger.assertNoErrors();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello World");
	}
}
