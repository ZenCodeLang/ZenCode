package org.openzen.zenscript.scriptingexample.tests.actual_test.java_native;

import org.junit.jupiter.api.Test;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.util.List;
import java.util.function.*;

public class SpecialFunctionalInterfaceTests extends ZenCodeTest {
	@Override
	public List<Class<?>> getRequiredClasses() {
		List<Class<?>> classes = super.getRequiredClasses();
		classes.add(Acceptor.class);
		return classes;
	}

	@Test
	void useBiConsumer() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("Acceptor.acceptBiConsumer((a, b) => {println('Inside BiConsumer: ' + a + ' ' + b);});")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside BiConsumer: biConsumer 5"
		);
	}

	@Test
	void useBiFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptBiFunction((a, b) => {println('Inside BiFunction: ' + a + ' ' + b); return b + ' ' + a;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside BiFunction: 5 biFunction",
				"biFunction 5"
		);
	}

	@Test
	void useBiPredicate() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptBiPredicate((a, b) => {println('Inside BiPredicate: ' + a + ' ' + b); return true;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside BiPredicate: 5 biPredicate",
				"true"
		);
	}

	@Test
	void useBooleanSupplier() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptBooleanSupplier(() => {println('Inside BooleanSupplier'); return true;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside BooleanSupplier",
				"true"
		);
	}

	@Test
	void useConsumer() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("Acceptor.acceptConsumer((a) => {println('Inside Consumer: ' + a);});")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside Consumer: consumer"
		);

	}

	@Test
	void useDoubleBinaryOperator() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptDoubleBinaryOperator((a, b) => {println('Inside DoubleBinaryOperator: ' + a + ' ' + b); return a + b;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside DoubleBinaryOperator: 2.7 3.14",
				"5.84"
		);
	}

	@Test
	void useDoubleConsumer() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("Acceptor.acceptDoubleConsumer((a) => {println('Inside DoubleConsumer: ' + a);});")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside DoubleConsumer: 3.14"
		);
	}

	@Test
	void useDoubleFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptDoubleFunction((a) => {println('Inside DoubleFunction: ' + a); return 'As string: ' + a;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside DoubleFunction: 3.14",
				"As string: 3.14"
		);
	}

	@Test
	void useDoublePredicate() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptDoublePredicate((a) => {println('Inside DoublePredicate: ' + a); return true;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside DoublePredicate: 3.14",
				"true"
		);
	}

	@Test
	void useDoubleSupplier() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptDoubleSupplier(() => {println('Inside DoubleSupplier'); return 6.28;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside DoubleSupplier",
				"6.28"
		);
	}

	@Test
	void useDoubleToIntFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptDoubleToIntFunction((a) => {println('Inside DoubleToIntFunction: ' + a); return 93;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside DoubleToIntFunction: 3.14",
				"93"
		);
	}

	@Test
	void useDoubleToLongFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptDoubleToLongFunction((a) => {println('Inside DoubleToLongFunction: ' + a); return 8528;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside DoubleToLongFunction: 3.14",
				"8528"
		);
	}

	@Test
	void useDoubleUnaryOperator() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptDoubleUnaryOperator((a) => {println('Inside DoubleUnaryOperator: ' + a); return a * 2d;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside DoubleUnaryOperator: 3.14",
				"6.28"
		);
	}

	@Test
	void useFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptFunction((a) => {println('Inside Function: ' + a); return a.length as int?;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside Function: function",
				"8"
		);
	}

	@Test
	void useIntBinaryOperator() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptIntBinaryOperator((a, b) => {println('Inside IntBinaryOperator: ' + a + ' ' + b); return a + b;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside IntBinaryOperator: 5 7",
				"12"
		);
	}

	@Test
	void useIntConsumer() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("Acceptor.acceptIntConsumer((a) => {println('Inside IntConsumer: ' + a);});")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside IntConsumer: 5"
		);
	}

	@Test
	void useIntFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptIntFunction((a) => {println('Inside IntFunction: ' + a); return 'As string: ' + a as string;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside IntFunction: 5",
				"As string: 5"
		);
	}

	@Test
	void useIntPredicate() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptIntPredicate((a) => {println('Inside IntPredicate: ' + a); return true;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside IntPredicate: 5",
				"true"
		);
	}

	@Test
	void useIntSupplier() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptIntSupplier(() => {println('Inside IntSupplier'); return 93;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside IntSupplier",
				"93"
		);
	}

	@Test
	void useIntToDoubleFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptIntToDoubleFunction((a) => {println('Inside IntToDoubleFunction: ' + a); return 853.45d; });")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside IntToDoubleFunction: 5",
				"853.45"
		);
	}

	@Test
	void useIntToLongFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptIntToLongFunction((a) => {println('Inside IntToLongFunction: ' + a); return 34730L;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside IntToLongFunction: 5",
				"34730"
		);
	}

	@Test
	void useIntUnaryOperator() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptIntUnaryOperator((a) => {println('Inside IntUnaryOperator: ' + a); return a * 2;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside IntUnaryOperator: 5",
				"10"
		);
	}

	@Test
	void useLongBinaryOperator() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptLongBinaryOperator((a, b) => {println('Inside LongBinaryOperator: ' + a + ' ' + b); return a + b;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside LongBinaryOperator: 42 47",
				"89"
		);
	}

	@Test
	void useLongConsumer() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("Acceptor.acceptLongConsumer((a) => {println('Inside LongConsumer: ' + a);});")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside LongConsumer: 47"
		);
	}

	@Test
	void useLongFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptLongFunction((a) => {println('Inside LongFunction: ' + a); return 'As string: ' + a as string;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside LongFunction: 5",
				"As string: 5"
		);
	}

	@Test
	void useLongPredicate() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptLongPredicate((a) => {println('Inside LongPredicate: ' + a); return true;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside LongPredicate: 5",
				"true"
		);
	}

	@Test
	void useLongSupplier() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptLongSupplier(() => {println('Inside LongSupplier'); return 93L;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside LongSupplier",
				"93"
		);
	}

	@Test
	void useLongToDoubleFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptLongToDoubleFunction((a) => {println('Inside LongToDoubleFunction: ' + a); return 666.523d;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside LongToDoubleFunction: 47",
				"666.523"
		);
	}

	@Test
	void useLongToIntFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptLongToIntFunction((a) => {println('Inside LongToIntFunction: ' + a); return 444567;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside LongToIntFunction: 5",
				"444567"
		);
	}

	@Test
	void useLongUnaryOperator() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptLongUnaryOperator((a) => {println('Inside LongUnaryOperator: ' + a); return a * 2;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside LongUnaryOperator: 47",
				"94"
		);
	}

	@Test
	void useObjDoubleConsumer() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("Acceptor.acceptObjDoubleConsumer((a, b) => {println('Inside ObjDoubleConsumer: ' + a + ' ' + b);});")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside ObjDoubleConsumer: objDoubleConsumer 3.14"
		);
	}

	@Test
	void useObjIntConsumer() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("Acceptor.acceptObjIntConsumer((a, b) => {println('Inside ObjIntConsumer: ' + a + ' ' + b);});")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside ObjIntConsumer: objIntConsumer 5"
		);
	}

	@Test
	void useObjLongConsumer() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("Acceptor.acceptObjLongConsumer((a, b) => {println('Inside ObjLongConsumer: ' + a + ' ' + b);});")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside ObjLongConsumer: objLongConsumer 47"
		);
	}

	@Test
	void usePredicate() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptPredicate((a) => {println('Inside Predicate: ' + a); return true;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside Predicate: predicate",
				"true"
		);
	}

	@Test
	void useSupplier() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptSupplier(() => {println('Inside Supplier'); return 'Hello from Supplier';});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside Supplier",
				"Hello from Supplier"
		);
	}

	@Test
	void useToDoubleBiFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptToDoubleBiFunction((a, b) => {println('Inside ToDoubleBiFunction: ' + a + ' ' + b); return 4535.97;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside ToDoubleBiFunction: toDoubleBiFunction 47",
				"4535.97"
		);
	}

	@Test
	void useToDoubleFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptToDoubleFunction((a) => {println('Inside ToDoubleFunction: ' + a); return 4535.98;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside ToDoubleFunction: toDoubleFunction",
				"4535.98"
		);
	}

	@Test
	void useToIntBiFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptToIntBiFunction((a, b) => {println('Inside ToIntBiFunction: ' + a + ' ' + b); return 4535;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside ToIntBiFunction: toIntBiFunction 3.14",
				"4535"
		);
	}

	@Test
	void useToIntFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptToIntFunction((a) => {println('Inside ToIntFunction: ' + a); return 4535;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside ToIntFunction: toIntFunction",
				"4535"
		);
	}

	@Test
	void useToLongBiFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptToLongBiFunction((a, b) => {println('Inside ToLongBiFunction: ' + a + ' ' + b); return 9263;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside ToLongBiFunction: toLongBiFunction 3.14",
				"9263"
		);
	}

	@Test
	void useToLongFunction() {
		ScriptBuilder.create()
				.add("import test_module.Acceptor;")
				.add("var result = Acceptor.acceptToLongFunction((a) => {println('Inside ToLongFunction: ' + a); return 9264;});")
				.add("println(result);")
				.execute(this);

		logger.printlnOutputs().assertLinesInOrder(
				"Inside ToLongFunction: toLongFunction",
				"9264"
		);
	}


	@ZenCodeType.Name("test_module.Acceptor")
	public static class Acceptor {
		@ZenCodeType.Method
		public static void acceptBiConsumer(BiConsumer<String, Integer> biConsumer) {
			biConsumer.accept("biConsumer", 5);
		}

		@ZenCodeType.Method
		public static String acceptBiFunction(BiFunction<Integer, String, String> biFunction) {
			return biFunction.apply(5, "biFunction");
		}

		@ZenCodeType.Method
		public static boolean acceptBiPredicate(BiPredicate<Integer, String> biPredicate) {
			return biPredicate.test(5, "biPredicate");
		}

		@ZenCodeType.Method
		public static boolean acceptBooleanSupplier(BooleanSupplier booleanSupplier) {
			return booleanSupplier.getAsBoolean();
		}

		@ZenCodeType.Method
		public static void acceptConsumer(Consumer<String> consumer) {
			consumer.accept("consumer");
		}

		@ZenCodeType.Method
		public static double acceptDoubleBinaryOperator(DoubleBinaryOperator doubleBinaryOperator) {
			return doubleBinaryOperator.applyAsDouble(2.7, 3.14);
		}

		@ZenCodeType.Method
		public static void acceptDoubleConsumer(DoubleConsumer doubleConsumer) {
			doubleConsumer.accept(3.14);
		}

		@ZenCodeType.Method
		public static String acceptDoubleFunction(DoubleFunction<String> doubleFunction) {
			return doubleFunction.apply(3.14);
		}

		@ZenCodeType.Method
		public static boolean acceptDoublePredicate(DoublePredicate doublePredicate) {
			return doublePredicate.test(3.14);
		}

		@ZenCodeType.Method
		public static double acceptDoubleSupplier(DoubleSupplier doubleSupplier) {
			return doubleSupplier.getAsDouble();
		}

		@ZenCodeType.Method
		public static int acceptDoubleToIntFunction(DoubleToIntFunction doubleToIntFunction) {
			return doubleToIntFunction.applyAsInt(3.14);
		}

		@ZenCodeType.Method
		public static long acceptDoubleToLongFunction(DoubleToLongFunction doubleToLongFunction) {
			return doubleToLongFunction.applyAsLong(3.14);
		}

		@ZenCodeType.Method
		public static double acceptDoubleUnaryOperator(DoubleUnaryOperator doubleUnaryOperator) {
			return doubleUnaryOperator.applyAsDouble(3.14);
		}

		@ZenCodeType.Method
		public static int acceptFunction(Function<String, Integer> function) {
			return function.apply("function");
		}

		@ZenCodeType.Method
		public static int acceptIntBinaryOperator(IntBinaryOperator intBinaryOperator) {
			return intBinaryOperator.applyAsInt(5, 7);
		}

		@ZenCodeType.Method
		public static void acceptIntConsumer(IntConsumer intConsumer) {
			intConsumer.accept(5);
		}

		@ZenCodeType.Method
		public static String acceptIntFunction(IntFunction<String> intFunction) {
			return intFunction.apply(5);
		}

		@ZenCodeType.Method
		public static boolean acceptIntPredicate(IntPredicate intPredicate) {
			return intPredicate.test(5);
		}

		@ZenCodeType.Method
		public static int acceptIntSupplier(IntSupplier intSupplier) {
			return intSupplier.getAsInt();
		}

		@ZenCodeType.Method
		public static double acceptIntToDoubleFunction(IntToDoubleFunction intToDoubleFunction) {
			return intToDoubleFunction.applyAsDouble(5);
		}

		@ZenCodeType.Method
		public static long acceptIntToLongFunction(IntToLongFunction intToLongFunction) {
			return intToLongFunction.applyAsLong(5);
		}

		@ZenCodeType.Method
		public static int acceptIntUnaryOperator(IntUnaryOperator intUnaryOperator) {
			return intUnaryOperator.applyAsInt(5);
		}

		@ZenCodeType.Method
		public static long acceptLongBinaryOperator(LongBinaryOperator longBinaryOperator) {
			return longBinaryOperator.applyAsLong(42, 47);
		}

		@ZenCodeType.Method
		public static void acceptLongConsumer(LongConsumer longConsumer) {
			longConsumer.accept(47);
		}

		@ZenCodeType.Method
		public static String acceptLongFunction(LongFunction<String> longFunction) {
			return longFunction.apply(5);
		}

		@ZenCodeType.Method
		public static boolean acceptLongPredicate(LongPredicate longPredicate) {
			return longPredicate.test(5);
		}

		@ZenCodeType.Method
		public static long acceptLongSupplier(LongSupplier longSupplier) {
			return longSupplier.getAsLong();
		}

		@ZenCodeType.Method
		public static double acceptLongToDoubleFunction(LongToDoubleFunction longToDoubleFunction) {
			return longToDoubleFunction.applyAsDouble(47);
		}

		@ZenCodeType.Method
		public static int acceptLongToIntFunction(LongToIntFunction longToIntFunction) {
			return longToIntFunction.applyAsInt(5);
		}

		@ZenCodeType.Method
		public static long acceptLongUnaryOperator(LongUnaryOperator longUnaryOperator) {
			return longUnaryOperator.applyAsLong(47);
		}

		@ZenCodeType.Method
		public static void acceptObjDoubleConsumer(ObjDoubleConsumer<String> objDoubleConsumer) {
			objDoubleConsumer.accept("objDoubleConsumer", 3.14);
		}

		@ZenCodeType.Method
		public static void acceptObjIntConsumer(ObjIntConsumer<String> objIntConsumer) {
			objIntConsumer.accept("objIntConsumer", 5);
		}

		@ZenCodeType.Method
		public static void acceptObjLongConsumer(ObjLongConsumer<String> objLongConsumer) {
			objLongConsumer.accept("objLongConsumer", 47);
		}

		@ZenCodeType.Method
		public static boolean acceptPredicate(Predicate<String> predicate) {
			return predicate.test("predicate");
		}

		@ZenCodeType.Method
		public static String acceptSupplier(Supplier<String> supplier) {
			return supplier.get();
		}

		@ZenCodeType.Method
		public static double acceptToDoubleBiFunction(ToDoubleBiFunction<String, Long> toDoubleBiFunction) {
			return toDoubleBiFunction.applyAsDouble("toDoubleBiFunction", 47L);
		}

		@ZenCodeType.Method
		public static double acceptToDoubleFunction(ToDoubleFunction<String> toDoubleFunction) {
			return toDoubleFunction.applyAsDouble("toDoubleFunction");
		}

		@ZenCodeType.Method
		public static int acceptToIntBiFunction(ToIntBiFunction<String, Double> toIntBiFunction) {
			return toIntBiFunction.applyAsInt("toIntBiFunction", 3.14);
		}

		@ZenCodeType.Method
		public static int acceptToIntFunction(ToIntFunction<String> toIntFunction) {
			return toIntFunction.applyAsInt("toIntFunction");
		}

		@ZenCodeType.Method
		public static long acceptToLongBiFunction(ToLongBiFunction<String, Double> toLongBiFunction) {
			return toLongBiFunction.applyAsLong("toLongBiFunction", 3.14);
		}

		@ZenCodeType.Method
		public static long acceptToLongFunction(ToLongFunction<String> toLongFunction) {
			return toLongFunction.applyAsLong("toLongFunction");
		}
	}
}
