package org.openzen.zenscript.scriptingexample.tests.actual_test.variants;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class VariantTestOptional extends ZenCodeTest {

	//21 lines in total
	private static ScriptBuilder createBuilder() {
		return ScriptBuilder.create()
				.add("public variant Optional<T> {")
				.add("    Present(T),")
				.add("    Empty;")
				.add("")
				.add("    public expect() as T {")
				.add("        return match this {")
				.add("            Present(value) => value,")
				.add("            Empty() => panic('Expect called on empty value')")
				.add("        };")
				.add("    }")
				.add("")
				//.add("    public ifPresent(consumer as function(value as T) as void) as void {")
				//.add("        if(this.isPresent) {")
				//.add("            consumer(this.expect());")
				//.add("        }")
				//.add("    }")
				.add("")
				.add("    public get isPresent as bool => match(this) {")
				.add("        Present(value) => true,")
				.add("        Empty => false")
				.add("    };")
				.add("}");
	}

	@Test
	public void testIsPresent() {
		createBuilder()
				.add("var opt as Optional<string> = Present('Hello World');")
				.add("println(opt.isPresent);")
				.add("opt = Empty;")
				.add("println(opt.isPresent);")
				.execute(this);
		logger.assertPrintOutput(0, "true");
		logger.assertPrintOutput(1, "false");
	}

	@Test
	public void testExpectPresent() {
		createBuilder()
				.add("var opt as Optional<string> = Present('Hello World');")
				.add("println(opt.expect());")
				.execute(this);
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello World");
	}

	@Test
	public void testExpectEmpty() {
		createBuilder()
				.add("var opt as Optional<string> = Empty;")
				.add("println(opt.expect());")
				.execute(this, ScriptBuilder.LogTolerance.ALLOW_ERRORS);
		logger.errors().assertSize(1);
		logger.errors().assertLine(0, "Expect called on empty value");
	}

	@Test
	@Disabled("Disabled until ifPresent properly compiles")
	public void testIfPresentPresent() {
		createBuilder()
				.add("var opt as Optional<string> = Present('Hello World');")
				.add("opt.ifPresent((value) => println(value));")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello World");
	}

	@Test
	@Disabled("Disabled until ifPresent properly compiles")
	public void testIfPresentEmpty() {
		createBuilder()
				.add("var opt as Optional<string> = Empty;")
				.add("opt.ifPresent((value) => println(value));")
				.execute(this);

		logger.assertPrintOutputSize(0);
	}
}
