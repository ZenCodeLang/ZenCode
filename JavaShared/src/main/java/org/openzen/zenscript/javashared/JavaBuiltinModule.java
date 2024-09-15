package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.type.builtin.*;

public class JavaBuiltinModule {
	public static final JavaNativeMethod OBJECT_HASHCODE = JavaNativeMethod.getNativeVirtual(JavaClass.OBJECT, "hashCode", "()I");
	public static final JavaNativeMethod OBJECT_EQUALS = JavaNativeMethod.getNativeVirtual(JavaClass.OBJECT, "equals", "(Ljava/lang/Object;)Z");
	public static final JavaNativeMethod OBJECT_CLONE = JavaNativeMethod.getNativeVirtual(JavaClass.OBJECT, "clone", "()Ljava/lang/Object;");
	private static final JavaNativeMethod OBJECTS_TOSTRING = JavaNativeMethod.getNativeStatic(new JavaClass("java.util", "Objects", JavaClass.Kind.CLASS), "toString", "(Ljava/lang/Object;)Ljava/lang/String;");
	private static final JavaNativeMethod BYTE_PARSE = JavaNativeMethod.getNativeStatic(JavaClass.BYTE, "parseByte", "(Ljava/lang/String;)B");
	private static final JavaNativeMethod BYTE_PARSE_WITH_BASE = JavaNativeMethod.getNativeStatic(JavaClass.BYTE, "parseByte", "(Ljava/lang/String;I)B");
	private static final JavaNativeField BYTE_MIN_VALUE = new JavaNativeField(JavaClass.BYTE, "MIN_VALUE", "B");
	private static final JavaNativeField BYTE_MAX_VALUE = new JavaNativeField(JavaClass.BYTE, "MAX_VALUE", "B");
	private static final JavaNativeMethod BYTE_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.BYTE, "toString", "(B)Ljava/lang/String;");
	private static final JavaNativeMethod SHORT_PARSE = JavaNativeMethod.getNativeStatic(JavaClass.SHORT, "parseShort", "(Ljava/lang/String;)S");
	private static final JavaNativeMethod SHORT_PARSE_WITH_BASE = JavaNativeMethod.getNativeStatic(JavaClass.SHORT, "parseShort", "(Ljava/lang/String;I)S");
	private static final JavaNativeField SHORT_MIN_VALUE = new JavaNativeField(JavaClass.SHORT, "MIN_VALUE", "S");
	private static final JavaNativeField SHORT_MAX_VALUE = new JavaNativeField(JavaClass.SHORT, "MAX_VALUE", "S");
	private static final JavaNativeMethod SHORT_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.SHORT, "toString", "(S)Ljava/lang/String;");
	public static final JavaNativeMethod INTEGER_COMPARE_UNSIGNED = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "compareUnsigned", "(II)I");
	private static final JavaNativeMethod INTEGER_DIVIDE_UNSIGNED = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "divideUnsigned", "(II)I");
	private static final JavaNativeMethod INTEGER_NUMBER_OF_TRAILING_ZEROS = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "numberOfTrailingZeros", "(I)I");
	private static final JavaNativeMethod INTEGER_NUMBER_OF_LEADING_ZEROS = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "numberOfLeadingZeros", "(I)I");
	private static final JavaNativeMethod INTEGER_PARSE = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "parseInt", "(Ljava/lang/String;)I");
	private static final JavaNativeMethod INTEGER_PARSE_WITH_BASE = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "parseInt", "(Ljava/lang/String;I)I");
	private static final JavaNativeMethod INTEGER_PARSE_UNSIGNED = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "parseUnsignedInt", "(Ljava/lang/String;)I");
	private static final JavaNativeMethod INTEGER_PARSE_UNSIGNED_WITH_BASE = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "parseUnsignedInt", "(Ljava/lang/String;I)I");
	private static final JavaNativeMethod INTEGER_HIGHEST_ONE_BIT = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "highestOneBit", "(I)I");
	private static final JavaNativeMethod INTEGER_LOWEST_ONE_BIT = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "lowestOneBit", "(I)I");
	private static final JavaNativeMethod INTEGER_BIT_COUNT = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "bitCount", "(I)I");
	private static final JavaNativeField INTEGER_MIN_VALUE = new JavaNativeField(JavaClass.INTEGER, "MIN_VALUE", "I");
	private static final JavaNativeField INTEGER_MAX_VALUE = new JavaNativeField(JavaClass.INTEGER, "MAX_VALUE", "I");
	private static final JavaNativeMethod INTEGER_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "toString", "(I)Ljava/lang/String;");
	private static final JavaNativeMethod INTEGER_TO_UNSIGNED_STRING = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "toUnsignedString", "(I)Ljava/lang/String;");
	public static final JavaNativeMethod LONG_COMPARE = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "compare", "(JJ)I");
	public static final JavaNativeMethod LONG_COMPARE_UNSIGNED = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "compareUnsigned", "(JJ)I");
	private static final JavaNativeMethod LONG_DIVIDE_UNSIGNED = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "divideUnsigned", "(JJ)J");
	private static final JavaNativeMethod LONG_REMAINDER_UNSIGNED = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "remainderUnsigned", "(JJ)J");
	private static final JavaNativeMethod LONG_NUMBER_OF_TRAILING_ZEROS = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "numberOfTrailingZeros", "(J)I");
	private static final JavaNativeMethod LONG_NUMBER_OF_LEADING_ZEROS = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "numberOfLeadingZeros", "(J)I");
	private static final JavaNativeMethod LONG_PARSE = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "parseLong", "(Ljava/lang/String;)J");
	private static final JavaNativeMethod LONG_PARSE_WITH_BASE = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "parseLong", "(Ljava/lang/String;I)J");
	private static final JavaNativeMethod LONG_PARSE_UNSIGNED = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "parseUnsignedLong", "(Ljava/lang/String;)J");
	private static final JavaNativeMethod LONG_PARSE_UNSIGNED_WITH_BASE = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "parseUnsignedLong", "(Ljava/lang/String;I)J");
	private static final JavaNativeMethod LONG_HIGHEST_ONE_BIT = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "highestOneBit", "(J)J");
	private static final JavaNativeMethod LONG_LOWEST_ONE_BIT = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "lowestOneBit", "(J)J");
	private static final JavaNativeMethod LONG_BIT_COUNT = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "bitCount", "(J)I");
	private static final JavaNativeField LONG_MIN_VALUE = new JavaNativeField(JavaClass.LONG, "MIN_VALUE", "J");
	private static final JavaNativeField LONG_MAX_VALUE = new JavaNativeField(JavaClass.LONG, "MAX_VALUE", "J");
	private static final JavaNativeMethod LONG_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "toString", "(J)Ljava/lang/String;");
	private static final JavaNativeMethod LONG_TO_UNSIGNED_STRING = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "toUnsignedString", "(J)Ljava/lang/String;");
	public static final JavaNativeMethod FLOAT_COMPARE = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "compare", "(FF)I");
	private static final JavaNativeMethod FLOAT_PARSE = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "parseFloat", "(Ljava/lang/String;)F");
	private static final JavaNativeMethod FLOAT_FROM_BITS = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "intBitsToFloat", "(I)F");
	private static final JavaNativeMethod FLOAT_BITS = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "floatToRawIntBits", "(F)I");
	private static final JavaNativeField FLOAT_MIN_VALUE = new JavaNativeField(JavaClass.FLOAT, "MIN_VALUE", "F");
	private static final JavaNativeField FLOAT_MAX_VALUE = new JavaNativeField(JavaClass.FLOAT, "MAX_VALUE", "F");
	private static final JavaNativeMethod FLOAT_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "toString", "(F)Ljava/lang/String;");
	public static final JavaNativeMethod DOUBLE_COMPARE = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "compare", "(DD)I");
	private static final JavaNativeMethod DOUBLE_PARSE = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "parseDouble", "(Ljava/lang/String;)D");
	private static final JavaNativeMethod DOUBLE_FROM_BITS = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "longBitsToDouble", "(J)D");
	private static final JavaNativeMethod DOUBLE_BITS = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "doubleToRawLongBits", "(D)J");
	private static final JavaNativeField DOUBLE_MIN_VALUE = new JavaNativeField(JavaClass.DOUBLE, "MIN_VALUE", "D");
	private static final JavaNativeField DOUBLE_MAX_VALUE = new JavaNativeField(JavaClass.DOUBLE, "MAX_VALUE", "D");
	private static final JavaNativeMethod DOUBLE_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "toString", "(D)Ljava/lang/String;");
	private static final JavaNativeMethod CHARACTER_TO_LOWER_CASE = JavaNativeMethod.getNativeVirtual(JavaClass.CHARACTER, "toLowerCase", "()C");
	private static final JavaNativeMethod CHARACTER_TO_UPPER_CASE = JavaNativeMethod.getNativeVirtual(JavaClass.CHARACTER, "toUpperCase", "()C");
	private static final JavaNativeField CHARACTER_MIN_VALUE = new JavaNativeField(JavaClass.CHARACTER, "MIN_VALUE", "C");
	private static final JavaNativeField CHARACTER_MAX_VALUE = new JavaNativeField(JavaClass.CHARACTER, "MAX_VALUE", "C");
	private static final JavaNativeMethod CHARACTER_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.CHARACTER, "toString", "(C)Ljava/lang/String;");
	private static final JavaNativeMethod STRING_INIT_CHARACTERS = JavaNativeMethod.getNativeConstructor(JavaClass.STRING, "([C)V");
	private static final JavaNativeMethod STRING_INIT_BYTES_CHARSET = JavaNativeMethod.getNativeConstructor(JavaClass.STRING, "([BLjava/nio/charset/Charset;)V");
	public static final JavaNativeMethod STRING_COMPARETO = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "compareTo", "(Ljava/lang/String;)I");
	private static final JavaNativeMethod STRING_CONCAT = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "concat", "(Ljava/lang/String;)Ljava/lang/String;");
	private static final JavaNativeMethod STRING_CHAR_AT = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "charAt", "(I)C");
	private static final JavaNativeMethod STRING_SUBSTRING = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "substring", "(II)Ljava/lang/String;");
	private static final JavaNativeMethod STRING_TRIM = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "trim", "()Ljava/lang/String;");
	private static final JavaNativeMethod STRING_TO_LOWER_CASE = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "toLowerCase", "()Ljava/lang/String;");
	private static final JavaNativeMethod STRING_TO_UPPER_CASE = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "toUpperCase", "()Ljava/lang/String;");
	private static final JavaNativeMethod STRING_ISEMPTY = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "isEmpty", "()Z");
	private static final JavaNativeMethod STRING_GET_BYTES = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "getBytes", "(Ljava/nio/charset/Charset;)[B");
	public static final JavaNativeMethod ENUM_COMPARETO = JavaNativeMethod.getNativeVirtual(JavaClass.ENUM, "compareTo", "(Ljava/lang/Enum;)I");
	private static final JavaNativeMethod ENUM_NAME = JavaNativeMethod.getNativeVirtual(JavaClass.ENUM, "name", "()Ljava/lang/String;");
	public static final JavaNativeMethod ENUM_ORDINAL = JavaNativeMethod.getNativeVirtual(JavaClass.ENUM, "ordinal", "()I");
	private static final JavaNativeMethod HASHMAP_INIT = JavaNativeMethod.getNativeConstructor(JavaClass.HASHMAP, "()V");
	private static final JavaNativeMethod MAP_GET = JavaNativeMethod.getInterface(JavaClass.MAP, "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
	private static final JavaNativeMethod MAP_PUT = JavaNativeMethod.getInterface(JavaClass.MAP, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
	private static final JavaNativeMethod MAP_PUT_ALL = JavaNativeMethod.getInterface(JavaClass.MAP, "putAll", "(Ljava/util/Map;)V");
	private static final JavaNativeMethod MAP_CONTAINS_KEY = JavaNativeMethod.getInterface(JavaClass.MAP, "containsKey", "(Ljava/lang/Object;)Z");
	private static final JavaNativeMethod MAP_SIZE = JavaNativeMethod.getInterface(JavaClass.MAP, "size", "()I");
	private static final JavaNativeMethod MAP_ISEMPTY = JavaNativeMethod.getInterface(JavaClass.MAP, "isEmpty", "()Z");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_OBJECTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([Ljava/lang/Object;II)[Ljava/lang/Object;");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_BOOLS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([ZII)[Z");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_BYTES = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([BII)[B");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_SHORTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([SII)[S");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_INTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([III)[I");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_LONGS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([JII)[J");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_FLOATS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([FII)[F");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_DOUBLES = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([DII)[D");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_CHARS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([CII)[C");
	private static final JavaNativeMethod ARRAYS_EQUALS_OBJECTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([Ljava/lang/Object;[Ljava/lang/Object;)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_BOOLS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([Z[Z)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_BYTES = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([B[B)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_SHORTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([S[S)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_INTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([I[I)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_LONGS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([J[J)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_FLOATS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([F[F)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_DOUBLES = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([D[D)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_CHARS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([C[C)Z");
	private static final JavaNativeMethod ARRAYS_DEEPHASHCODE = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "deepHashCode", "([Ljava/lang/Object;)");
	private static final JavaNativeMethod ARRAYS_HASHCODE_BOOLS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([Z)I");
	private static final JavaNativeMethod ARRAYS_HASHCODE_BYTES = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([B)I");
	private static final JavaNativeMethod ARRAYS_HASHCODE_SHORTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([S)I");
	private static final JavaNativeMethod ARRAYS_HASHCODE_INTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([I)I");
	private static final JavaNativeMethod ARRAYS_HASHCODE_LONGS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([J)I");
	private static final JavaNativeMethod ARRAYS_HASHCODE_FLOATS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([F)I");
	private static final JavaNativeMethod ARRAYS_HASHCODE_DOUBLES = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([D)I");
	private static final JavaNativeMethod ARRAYS_HASHCODE_CHARS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([C)I");
	private static final JavaNativeMethod COLLECTION_SIZE = JavaNativeMethod.getNativeVirtual(JavaClass.COLLECTION, "size", "()I");
	private static final JavaNativeMethod COLLECTION_TOARRAY = JavaNativeMethod.getNativeVirtual(JavaClass.COLLECTION, "toArray", "([Ljava/lang/Object;)[Ljava/lang/Object;");

	public static JavaCompiledModule generate() {
		JavaCompiledModule result = new JavaCompiledModule(ModuleSymbol.BUILTIN, FunctionParameter.NONE);
		for (BuiltinFieldSymbol builtin : BuiltinFieldSymbol.values()) {
			result.setFieldInfo(builtin, new JavaBuiltinField(builtin));
		}
		for (BuiltinMethodSymbol builtin : BuiltinMethodSymbol.values()) {
			result.setMethodInfo(builtin, new JavaBuiltinMethod(builtin));
		}

		result.setMethodInfo(BuiltinMethodSymbol.BOOL_TO_STRING, JavaNativeMethod.getNativeStatic(JavaClass.BOOLEAN, "toString", "(Z)Ljava/lang/String;"));
		result.setMethodInfo(BuiltinMethodSymbol.BOOL_PARSE, JavaNativeMethod.getNativeStatic(JavaClass.BOOLEAN, "parseBoolean", "(Ljava/lang/String;)Z"));

		JavaNativeMethod divideUnsigned = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "divideUnsigned", "(II)I");
		result.setMethodInfo(BuiltinMethodSymbol.BYTE_DIV_BYTE, divideUnsigned);
		result.setMethodInfo(BuiltinMethodSymbol.USHORT_DIV_USHORT, divideUnsigned);
		result.setMethodInfo(BuiltinMethodSymbol.UINT_DIV_UINT, divideUnsigned);

		result.setFieldInfo(BuiltinFieldSymbol.SBYTE_MIN_VALUE, BYTE_MIN_VALUE);
		result.setFieldInfo(BuiltinFieldSymbol.SBYTE_MAX_VALUE, BYTE_MAX_VALUE);

		result.setFieldInfo(BuiltinFieldSymbol.SHORT_MIN_VALUE, SHORT_MIN_VALUE);
		result.setFieldInfo(BuiltinFieldSymbol.SHORT_MAX_VALUE, SHORT_MAX_VALUE);

		result.setFieldInfo(BuiltinFieldSymbol.INT_MIN_VALUE, INTEGER_MIN_VALUE);
		result.setFieldInfo(BuiltinFieldSymbol.INT_MAX_VALUE, INTEGER_MAX_VALUE);

		result.setFieldInfo(BuiltinFieldSymbol.LONG_MIN_VALUE, LONG_MIN_VALUE);
		result.setFieldInfo(BuiltinFieldSymbol.LONG_MAX_VALUE, LONG_MAX_VALUE);

		JavaNativeMethod remainderUnsigned = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "remainderUnsigned", "(II)I");
		result.setMethodInfo(BuiltinMethodSymbol.BYTE_MOD_BYTE, remainderUnsigned);
		result.setMethodInfo(BuiltinMethodSymbol.USHORT_MOD_USHORT, remainderUnsigned);
		result.setMethodInfo(BuiltinMethodSymbol.UINT_MOD_UINT, remainderUnsigned);

		JavaNativeMethod intHighestOneBit = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "highestOneBit", "(I)I");
		result.setMethodInfo(BuiltinMethodSymbol.INT_HIGHEST_ONE_BIT, intHighestOneBit);
		result.setMethodInfo(BuiltinMethodSymbol.UINT_HIGHEST_ONE_BIT, intHighestOneBit);
		result.setMethodInfo(BuiltinMethodSymbol.USIZE_HIGHEST_ONE_BIT, intHighestOneBit);

		JavaNativeMethod intLowestOneBit = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "lowestOneBit", "(I)I");
		result.setMethodInfo(BuiltinMethodSymbol.INT_LOWEST_ONE_BIT, intLowestOneBit);
		result.setMethodInfo(BuiltinMethodSymbol.UINT_LOWEST_ONE_BIT, intLowestOneBit);
		result.setMethodInfo(BuiltinMethodSymbol.USIZE_LOWEST_ONE_BIT, intLowestOneBit);

		JavaNativeMethod intBitCount = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "bitCount", "(I)I");
		result.setMethodInfo(BuiltinMethodSymbol.INT_BIT_COUNT, intBitCount);
		result.setMethodInfo(BuiltinMethodSymbol.UINT_BIT_COUNT, intBitCount);
		result.setMethodInfo(BuiltinMethodSymbol.USIZE_BIT_COUNT, intBitCount);

		result.setMethodInfo(BuiltinMethodSymbol.UINT_COMPARE, INTEGER_COMPARE_UNSIGNED);
		result.setMethodInfo(BuiltinMethodSymbol.USIZE_COMPARE, INTEGER_COMPARE_UNSIGNED);

		JavaNativeMethod longHighestOneBit = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "highestOneBit", "(J)J");
		result.setMethodInfo(BuiltinMethodSymbol.LONG_HIGHEST_ONE_BIT, longHighestOneBit);
		result.setMethodInfo(BuiltinMethodSymbol.ULONG_HIGHEST_ONE_BIT, longHighestOneBit);

		JavaNativeMethod longLowestOneBit = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "lowestOneBit", "(J)J");
		result.setMethodInfo(BuiltinMethodSymbol.LONG_LOWEST_ONE_BIT, longLowestOneBit);
		result.setMethodInfo(BuiltinMethodSymbol.ULONG_LOWEST_ONE_BIT, longLowestOneBit);

		JavaNativeMethod longBitCount = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "bitCount", "(J)I");
		result.setMethodInfo(BuiltinMethodSymbol.LONG_BIT_COUNT, longBitCount);
		result.setMethodInfo(BuiltinMethodSymbol.ULONG_BIT_COUNT, longBitCount);

		JavaNativeMethod longToString = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "toString", "(J)Ljava/lang/String;");
		result.setMethodInfo(BuiltinMethodSymbol.LONG_TO_STRING, longToString);
		result.setMethodInfo(BuiltinMethodSymbol.LONG_COMPARE, LONG_COMPARE);

		JavaNativeMethod ulongToString = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "toUnsignedString", "(J)Ljava/lang/String;");
		result.setMethodInfo(BuiltinMethodSymbol.ULONG_TO_STRING, ulongToString);
		result.setMethodInfo(BuiltinMethodSymbol.ULONG_COMPARE, LONG_COMPARE_UNSIGNED);

		JavaNativeMethod floatBits = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "floatToRawIntBits", "(F)I");
		result.setMethodInfo(BuiltinMethodSymbol.FLOAT_BITS, floatBits);
		JavaNativeMethod floatCompare = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "compare", "(FF)I");
		result.setMethodInfo(BuiltinMethodSymbol.FLOAT_COMPARE, floatCompare);
		JavaNativeMethod floatFromBits = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "intBitsToFloat", "(I)F");
		result.setMethodInfo(BuiltinMethodSymbol.FLOAT_FROM_BITS, floatFromBits);
		JavaNativeMethod floatParse = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "parseFloat", "(Ljava/lang/String;)F");
		result.setMethodInfo(BuiltinMethodSymbol.FLOAT_PARSE, floatParse);
		JavaNativeMethod floatToString = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "toString", "(F)Ljava/lang/String;");
		result.setMethodInfo(BuiltinMethodSymbol.FLOAT_TO_STRING, floatToString);
		result.setMethodInfo(BuiltinMethodSymbol.FLOAT_COMPARE, FLOAT_COMPARE);

		JavaNativeField floatMinValue = new JavaNativeField(JavaClass.FLOAT, "MIN_VALUE", "F");
		result.setFieldInfo(BuiltinFieldSymbol.FLOAT_MIN_VALUE, floatMinValue);
		JavaNativeField floatMaxValue = new JavaNativeField(JavaClass.FLOAT, "MAX_VALUE", "F");
		result.setFieldInfo(BuiltinFieldSymbol.FLOAT_MAX_VALUE, floatMaxValue);

		JavaNativeMethod doubleBits = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "doubleToRawLongBits", "(D)J");
		result.setMethodInfo(BuiltinMethodSymbol.DOUBLE_BITS, doubleBits);
		JavaNativeMethod doubleCompare = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "compare", "(DD)I");
		result.setMethodInfo(BuiltinMethodSymbol.DOUBLE_COMPARE, doubleCompare);
		JavaNativeMethod doubleFromBits = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "longBitsToDouble", "(J)D");
		result.setMethodInfo(BuiltinMethodSymbol.DOUBLE_FROM_BITS, doubleFromBits);
		JavaNativeMethod doubleParse = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "parseDouble", "(Ljava/lang/String;)D");
		result.setMethodInfo(BuiltinMethodSymbol.DOUBLE_PARSE, doubleParse);
		JavaNativeMethod doubleToString = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "toString", "(D)Ljava/lang/String;");
		result.setMethodInfo(BuiltinMethodSymbol.DOUBLE_TO_STRING, doubleToString);

		JavaNativeField doubleMinValue = new JavaNativeField(JavaClass.DOUBLE, "MIN_VALUE", "D");
		result.setFieldInfo(BuiltinFieldSymbol.DOUBLE_MIN_VALUE, doubleMinValue);
		JavaNativeField doubleMaxValue = new JavaNativeField(JavaClass.DOUBLE, "MAX_VALUE", "D");
		result.setFieldInfo(BuiltinFieldSymbol.DOUBLE_MAX_VALUE, doubleMaxValue);

		result.setMethodInfo(BuiltinMethodSymbol.STRING_ISEMPTY, STRING_ISEMPTY);
		JavaNativeMethod stringLength = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "length", "()I");
		result.setMethodInfo(BuiltinMethodSymbol.STRING_LENGTH, stringLength);
		result.setMethodInfo(BuiltinMethodSymbol.STRING_DOLLAR, stringLength);
		result.setMethodInfo(BuiltinMethodSymbol.STRING_TO_UPPER_CASE, STRING_TO_UPPER_CASE);
		result.setMethodInfo(BuiltinMethodSymbol.STRING_TO_LOWER_CASE, STRING_TO_LOWER_CASE);

		JavaNativeMethod stringCharacters = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "toCharArray", "()[C");
		result.setMethodInfo(BuiltinMethodSymbol.STRING_CHARACTERS, stringCharacters);

		result.setMethodInfo(BuiltinMethodSymbol.STRING_COMPARE, STRING_COMPARETO);
		result.setMethodInfo(BuiltinMethodSymbol.STRING_CONSTRUCTOR_CHARACTERS, STRING_INIT_CHARACTERS);

		result.setMethodInfo(BuiltinMethodSymbol.ASSOC_SIZE, MAP_SIZE);
		result.setMethodInfo(BuiltinMethodSymbol.ASSOC_ISEMPTY, MAP_ISEMPTY);
		result.setMethodInfo(BuiltinMethodSymbol.ASSOC_HASHCODE, OBJECT_HASHCODE);

		result.setMethodInfo(BuiltinMethodSymbol.GENERICMAP_HASHCODE, OBJECT_HASHCODE);

		JavaNativeMethod enumName = JavaNativeMethod.getNativeVirtual(JavaClass.ENUM, "name", "()Ljava/lang/String;");
		result.setMethodInfo(BuiltinMethodSymbol.ENUM_NAME, enumName);
		result.setMethodInfo(BuiltinMethodSymbol.ENUM_TO_STRING, enumName);
		result.setMethodInfo(BuiltinMethodSymbol.ENUM_COMPARE, ENUM_COMPARETO);

		result.setMethodInfo(BuiltinMethodSymbol.ENUM_ORDINAL, ENUM_ORDINAL);

		result.setMethodInfo(BuiltinMethodSymbol.OBJECT_HASHCODE, OBJECT_HASHCODE);

		return result;
	}

	private JavaBuiltinModule() {}
}
