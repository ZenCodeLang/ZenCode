package org.openzen.zenscript.scriptingexample.tests.actual_test.enums;

import org.junit.jupiter.api.Test;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

public class EnumMembers extends ZenCodeTest {

	@Test
	public void nameMember() {
		ScriptBuilder.create()
				.add("public enum MyEnum {")
				.add("    A, B, C;")
				.add("    this(){}")
				.add("}")
				.add("println(MyEnum.B.name);")
				.add("println(MyEnum.A.name);")
				.add("println(MyEnum.C.name);")
				.execute(this);

		logger.assertPrintOutputSize(3);
		logger.assertPrintOutput(0, "B");
		logger.assertPrintOutput(1, "A");
		logger.assertPrintOutput(2, "C");
	}

	@Test
	public void ordinalMember() {
		ScriptBuilder.create()
				.add("public enum MyEnum {")
				.add("    A, B, C;")
				.add("    this(){}")
				.add("}")
				.add("println(MyEnum.B.ordinal);")
				.add("println(MyEnum.A.ordinal);")
				.add("println(MyEnum.C.ordinal);")
				.execute(this);

		logger.assertPrintOutputSize(3);
		logger.assertPrintOutput(0, "1");
		logger.assertPrintOutput(1, "0");
		logger.assertPrintOutput(2, "2");
	}

	@Test
	public void useNameAsExpression() {
		ScriptBuilder.create()
				.add("public enum MyEnum {")
				.add("    A, B, C;")
				.add("    this(){}")
				.add("}")
				.add("var x as MyEnum = B;")
				.add("println(x.name);")
				.execute(this);

		logger.assertPrintOutput(0, "B");
	}

	@Test
	public void valuesContainsMembersInOrder() {
		ScriptBuilder.create()
				.add("public enum MyEnum {")
				.add("    A, B, C;")
				.add("    this(){}")
				.add("}")
				.add("for e in MyEnum.values")
				.add("    println(e.name);")
				.execute(this);

		logger.assertPrintOutputSize(3);
		logger.assertPrintOutput(0, "A");
		logger.assertPrintOutput(1, "B");
		logger.assertPrintOutput(2, "C");
	}

	@Test
	public void implicitConstructorIsCreated() {
		ScriptBuilder.create()
				.add("public enum MyEnum {")
				.add("    A, B, C;")
				.add("}")
				.add("println(MyEnum.A.name);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "A");
	}

	@Test
	public void complexEnumMembers() {
		ScriptBuilder.create()
				.add("public enum MyEnum {")
				.add("    A('X1'), B('Y1'), C('Z1');")
				.add("    this(symbol as string) {this.symbol = symbol;}")
				.add("    public var symbol as string;")
				.add("}")
				.add("println(MyEnum.A.symbol);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "X1");
	}
}
