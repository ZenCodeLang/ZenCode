package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native.operators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTestLogger;

import java.util.List;
import java.util.stream.Stream;

public class OperatorCompare extends ZenCodeTest {

	public static Stream<Arguments> getTestCases() {
		final Stream.Builder<Arguments> builder = Stream.builder();
		final int[] leftOperands = {1, 2, 3};
		final int[] rightOperands = {2};

		for (int leftOperand : leftOperands) {
			for (int rightOperand : rightOperands) {
				builder.add(Arguments.of(CompareType.EQ.str, leftOperand, rightOperand, leftOperand == rightOperand));
				builder.add(Arguments.of(CompareType.NE.str, leftOperand, rightOperand, leftOperand != rightOperand));
				builder.add(Arguments.of(CompareType.GT.str, leftOperand, rightOperand, leftOperand > rightOperand));
				builder.add(Arguments.of(CompareType.GE.str, leftOperand, rightOperand, leftOperand >= rightOperand));
				builder.add(Arguments.of(CompareType.LT.str, leftOperand, rightOperand, leftOperand < rightOperand));
				builder.add(Arguments.of(CompareType.LE.str, leftOperand, rightOperand, leftOperand <= rightOperand));
			}
		}
		return builder.build();
	}

	@Override
	public List<Class<?>> getRequiredClasses() {
		final List<Class<?>> requiredClasses = super.getRequiredClasses();
		requiredClasses.add(ClassWithOperatorCompare.class);
		requiredClasses.add(ClassWithCompareAndEquals.class);
		return requiredClasses;
	}

	@BeforeEach
	public void before() {
		ClassWithCompareAndEquals.logger = this.logger;
	}

	@ParameterizedTest(name = "{1} {0} {2} == {3}")
	@MethodSource("getTestCases")
	@Disabled("Test is invalid due to Object == already being an operator.")
	public void testOperatorWorks(String type, int a, int b, boolean compareResult) {
		ScriptBuilder.create()
				.add("var a = createWithCompare(" + a + ");")
				.add("var b = createWithCompare(" + b + ");")
				.add("println(a " + type + "b);")
				.execute(this);
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, Boolean.toString(compareResult));
	}

	@Test
	public void testEqualsOperatorUsedWhenPresentInsteadOfCompare() {
		ScriptBuilder.create()
				.add("var a = createWithCompareAndEquals(1);")
				.add("var b = createWithCompareAndEquals(2);")
				.add("println(a == b);")
				.add("println(a != b);")
				.add("println(a > b);")
				.add("println(a >= b);")
				.add("println(a < b);")
				.add("println(a <= b);")
				.execute(this);
		logger.assertPrintOutputSize(12);
		logger.assertPrintOutput(0, "equals");
		logger.assertPrintOutput(1, "false");
		logger.assertPrintOutput(2, "equals");
		logger.assertPrintOutput(3, "true");
		logger.assertPrintOutput(4, "compare");
		logger.assertPrintOutput(5, "false");
		logger.assertPrintOutput(6, "compare");
		logger.assertPrintOutput(7, "false");
		logger.assertPrintOutput(8, "compare");
		logger.assertPrintOutput(9, "true");
		logger.assertPrintOutput(10, "compare");
		logger.assertPrintOutput(11, "true");
	}

	@ZenCodeType.Name("test_module.java_native.operator.compare.ClassWithOperatorCompare")
	public static final class ClassWithOperatorCompare {

		private final int i;

		public ClassWithOperatorCompare(int i) {
			this.i = i;
		}

		@ZenCodeGlobals.Global
		public static ClassWithOperatorCompare createWithCompare(int i) {
			return new ClassWithOperatorCompare(i);
		}

		@ZenCodeType.Operator(ZenCodeType.OperatorType.COMPARE)
		public int compare(ClassWithOperatorCompare other) {
			return Integer.compare(this.i, other.i);
		}
	}

	@ZenCodeType.Name("test_module.java_native.operator.compare.ClassWithCompareAndEquals")
	public static final class ClassWithCompareAndEquals {

		private static ZenCodeTestLogger logger;

		private final int i;

		public ClassWithCompareAndEquals(int i) {
			this.i = i;
		}

		@ZenCodeGlobals.Global
		public static ClassWithCompareAndEquals createWithCompareAndEquals(int i) {
			return new ClassWithCompareAndEquals(i);
		}

		@ZenCodeType.Operator(ZenCodeType.OperatorType.COMPARE)
		public int compare(ClassWithCompareAndEquals other) {
			logger.logPrintln("compare");
			return Integer.compare(this.i, other.i);
		}

		@ZenCodeType.Operator(ZenCodeType.OperatorType.EQUALS)
		public boolean equals(ClassWithCompareAndEquals other) {
			logger.logPrintln("equals");
			return this.i == other.i;
		}
	}
}
