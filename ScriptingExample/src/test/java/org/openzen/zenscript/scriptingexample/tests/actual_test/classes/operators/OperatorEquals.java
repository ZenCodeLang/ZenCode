package org.openzen.zenscript.scriptingexample.tests.actual_test.classes.operators;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class OperatorEquals extends ZenCodeTest {
	@Test
	public void canUseEqualsOperator() {
		ScriptBuilder.create()
				.add("public class A {")
				.add("    private val value as string;")
				.add("    public this(value as string) {")
				.add("        this.value = value;")
				.add("    }")
				.add("    public ==(other as A) as bool => this.value == other.value;")
				.add("}")
				.add("var a = new A('A');")
				.add("var b = new A('B');")
				.add("var a2 = new A('A');")
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
				.add("public class A {")
				.add("    private val value as string;")
				.add("    public this(value as string) {")
				.add("        this.value = value;")
				.add("    }")
				.add("    public ==(other as A) as bool => this.value == other.value;")
				.add("}")
				.add("var a = new A('A');")
				.add("var b = new A('B');")
				.add("var a2 = new A('A');")
				.add("println(a != b);")
				.add("println(a != a2);")
				.execute(this);

		logger.assertPrintOutputSize(2);
		logger.assertPrintOutput(0, "true");
		logger.assertPrintOutput(1, "false");
	}
}
