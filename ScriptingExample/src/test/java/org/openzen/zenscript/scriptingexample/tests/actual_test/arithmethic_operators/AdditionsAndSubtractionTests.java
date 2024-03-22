package org.openzen.zenscript.scriptingexample.tests.actual_test.arithmethic_operators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.*;
import java.util.stream.Stream;

public class AdditionsAndSubtractionTests extends ZenCodeTest {

	@ParameterizedTest(name = "{0} + {1}")
	@CsvSource({"0,0", "0,1", "10,10", "-1,1", "815,4711"})
	public void testSomeAdditions(int i, int j) {
		addScript(String.format("println(%d + %d);", i, j));
		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, String.valueOf(i + j));
	}

	@Test
	public void testChainedAdditions() {
		addScript("println(1+2+3+4+5+6+7+8+9+10);");
		executeEngine();

		logger.assertNoErrors();
		logger.assertNoWarnings();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, String.valueOf(55));
	}

	@Test
	public void testChainedSubtractions() {
		ScriptBuilder.create()
				.add("println(1-2-3-4-5-6-7-8-9-10);")
				.execute(this);

		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, String.valueOf(-53));
	}

	@ParameterizedTest
	@MethodSource("numberTypes")
	public void additionWorksWithDifferentTypes(String typeLeft, String typeRight) {
		ScriptBuilder.create()
				.add("var left   = 13 as " + typeLeft + ";")
				.add("var right  = 29 as " + typeRight + ";")
				.add("var result = left + right;")
				.add("println(result);")
				.execute(this);

		logger.assertPrintOutput(0, isResultFloatingPoint(typeLeft, typeRight) ? "42.0" : "42");
	}

	@ParameterizedTest
	@MethodSource("numberTypes")
	public void subtractionWorksWithDifferentTypes(String typeLeft, String typeRight) {
		ScriptBuilder.create()
				.add("var left   = 29 as " + typeLeft + ";")
				.add("var right  = 13 as " + typeRight + ";")
				.add("var result = left - right;")
				.add("println(result);")
				.execute(this);

		logger.assertPrintOutput(0, isResultFloatingPoint(typeLeft, typeRight) ? "16.0" : "16");
	}

	public static Stream<Arguments> numberTypes() {
		Stream.Builder<Arguments> builder = Stream.builder();
		List<String> types = Arrays.asList("byte", "sbyte", "short", "ushort", "int", "uint", "long", "ulong", "usize", "float", "double");


		for (String leftType : types) {
			for (String rightType : types) {
				if (invalidCombinations.contains(new Pair<>(leftType, rightType)) || invalidCombinations.contains(new Pair<>(rightType, leftType)))
					continue;
				builder.add(Arguments.of(leftType, rightType));
			}
		}

		return builder.build();
	}

	private static final Set<Pair<String>> invalidCombinations = new HashSet<>();
	static {
		invalidCombinations.add(new Pair<>("byte", "sbyte"));
		invalidCombinations.add(new Pair<>("short", "ushort"));
		invalidCombinations.add(new Pair<>("int", "uint"));
		invalidCombinations.add(new Pair<>("long", "ulong"));
		invalidCombinations.add(new Pair<>("usize", "int"));
		invalidCombinations.add(new Pair<>("usize", "long"));

		// Signed + unsigned combinations yet to be looked into
		invalidCombinations.add(new Pair<>("sbyte", "ushort"));
		invalidCombinations.add(new Pair<>("sbyte", "uint"));
		invalidCombinations.add(new Pair<>("sbyte", "ulong"));
		invalidCombinations.add(new Pair<>("sbyte", "usize"));
		invalidCombinations.add(new Pair<>("short", "byte"));
		invalidCombinations.add(new Pair<>("short", "uint"));
		invalidCombinations.add(new Pair<>("short", "ulong"));
		invalidCombinations.add(new Pair<>("short", "usize"));
		invalidCombinations.add(new Pair<>("int", "byte"));
		invalidCombinations.add(new Pair<>("int", "ushort"));
		invalidCombinations.add(new Pair<>("int", "ulong"));
		invalidCombinations.add(new Pair<>("int", "usize"));
		invalidCombinations.add(new Pair<>("long", "byte"));
		invalidCombinations.add(new Pair<>("long", "ushort"));
		invalidCombinations.add(new Pair<>("long", "uint"));
		invalidCombinations.add(new Pair<>("long", "usize"));
	}

	private static boolean isResultFloatingPoint(String leftType, String rightType) {
		Set<String> floatingPointTypes = new HashSet<>(Arrays.asList("float", "double"));
		return floatingPointTypes.contains(leftType) || floatingPointTypes.contains(rightType);
	}

	private static class Pair<T> {
		public final T left;
		public final T right;

		public Pair(T left, T right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Pair))
				return false;

			Pair<?> other = (Pair<?>) obj;
			return Objects.equals(left, other.left) && Objects.equals(right, other.right);
		}

		@Override
		public int hashCode() {
			return Objects.hash(left, right);
		}
	}
}