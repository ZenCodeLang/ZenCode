package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.RangeExpression;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javashared.*;

public class JavaMethodBytecodeCompiler implements JavaMethodCompiler<Void> {
	public static final JavaNativeMethod OBJECT_HASHCODE = JavaNativeMethod.getNativeVirtual(JavaClass.OBJECT, "hashCode", "()I");
	public static final JavaNativeMethod OBJECT_EQUALS = JavaNativeMethod.getNativeVirtual(JavaClass.OBJECT, "equals", "(Ljava/lang/Object)Z");
	public static final JavaNativeMethod OBJECT_CLONE = JavaNativeMethod.getNativeVirtual(JavaClass.OBJECT, "clone", "()Ljava/lang/Object;");
	private static final JavaNativeMethod OBJECTS_TOSTRING = JavaNativeMethod.getNativeStatic(new JavaClass("java.util", "Objects", JavaClass.Kind.CLASS), "toString", "(Ljava/lang/Object;)Ljava/lang/String;");
	private static final JavaNativeMethod BYTE_PARSE = JavaNativeMethod.getNativeStatic(JavaClass.BYTE, "parseByte", "(Ljava/lang/String;)B");
	private static final JavaNativeMethod BYTE_PARSE_WITH_BASE = JavaNativeMethod.getNativeStatic(JavaClass.BYTE, "parseByte", "(Ljava/lang/String;I)B");
	private static final JavaField BYTE_MIN_VALUE = new JavaField(JavaClass.BYTE, "MIN_VALUE", "B");
	private static final JavaField BYTE_MAX_VALUE = new JavaField(JavaClass.BYTE, "MAX_VALUE", "B");
	private static final JavaNativeMethod BYTE_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.BYTE, "toString", "(B)Ljava/lang/String;");
	private static final JavaNativeMethod SHORT_PARSE = JavaNativeMethod.getNativeStatic(JavaClass.SHORT, "parseShort", "(Ljava/lang/String;)S");
	private static final JavaNativeMethod SHORT_PARSE_WITH_BASE = JavaNativeMethod.getNativeStatic(JavaClass.SHORT, "parseShort", "(Ljava/lang/String;I)S");
	private static final JavaField SHORT_MIN_VALUE = new JavaField(JavaClass.SHORT, "MIN_VALUE", "S");
	private static final JavaField SHORT_MAX_VALUE = new JavaField(JavaClass.SHORT, "MAX_VALUE", "S");
	private static final JavaNativeMethod SHORT_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.SHORT, "toString", "(S)Ljava/lang/String;");
	private static final JavaNativeMethod INTEGER_COMPARE_UNSIGNED = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "compareUnsigned", "(II)I");
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
	private static final JavaField INTEGER_MIN_VALUE = new JavaField(JavaClass.INTEGER, "MIN_VALUE", "I");
	private static final JavaField INTEGER_MAX_VALUE = new JavaField(JavaClass.INTEGER, "MAX_VALUE", "I");
	private static final JavaNativeMethod INTEGER_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "toString", "(I)Ljava/lang/String;");
	private static final JavaNativeMethod INTEGER_TO_UNSIGNED_STRING = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "toUnsignedString", "(I)Ljava/lang/String;");
	private static final JavaNativeMethod LONG_COMPARE = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "compare", "(JJ)I");
	private static final JavaNativeMethod LONG_COMPARE_UNSIGNED = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "compareUnsigned", "(JJ)I");
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
	private static final JavaField LONG_MIN_VALUE = new JavaField(JavaClass.LONG, "MIN_VALUE", "J");
	private static final JavaField LONG_MAX_VALUE = new JavaField(JavaClass.LONG, "MAX_VALUE", "J");
	private static final JavaNativeMethod LONG_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "toString", "(J)Ljava/lang/String;");
	private static final JavaNativeMethod LONG_TO_UNSIGNED_STRING = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "toUnsignedString", "(J)Ljava/lang/String;");
	private static final JavaNativeMethod FLOAT_COMPARE = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "compare", "(FF)I");
	private static final JavaNativeMethod FLOAT_PARSE = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "parseFloat", "(Ljava/lang/String;)F");
	private static final JavaNativeMethod FLOAT_FROM_BITS = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "intBitsToFloat", "(I)F");
	private static final JavaNativeMethod FLOAT_BITS = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "floatToRawIntBits", "(F)I");
	private static final JavaField FLOAT_MIN_VALUE = new JavaField(JavaClass.FLOAT, "MIN_VALUE", "F");
	private static final JavaField FLOAT_MAX_VALUE = new JavaField(JavaClass.FLOAT, "MAX_VALUE", "F");
	private static final JavaNativeMethod FLOAT_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "toString", "(F)Ljava/lang/String;");
	private static final JavaNativeMethod DOUBLE_COMPARE = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "compare", "(DD)I");
	private static final JavaNativeMethod DOUBLE_PARSE = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "parseDouble", "(Ljava/lang/String;)D");
	private static final JavaNativeMethod DOUBLE_FROM_BITS = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "longBitsToDouble", "(J)D");
	private static final JavaNativeMethod DOUBLE_BITS = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "doubleToRawLongBits", "(D)J");
	private static final JavaField DOUBLE_MIN_VALUE = new JavaField(JavaClass.DOUBLE, "MIN_VALUE", "D");
	private static final JavaField DOUBLE_MAX_VALUE = new JavaField(JavaClass.DOUBLE, "MAX_VALUE", "D");
	private static final JavaNativeMethod DOUBLE_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "toString", "(D)Ljava/lang/String;");
	private static final JavaNativeMethod CHARACTER_TO_LOWER_CASE = JavaNativeMethod.getNativeVirtual(JavaClass.CHARACTER, "toLowerCase", "()C");
	private static final JavaNativeMethod CHARACTER_TO_UPPER_CASE = JavaNativeMethod.getNativeVirtual(JavaClass.CHARACTER, "toUpperCase", "()C");
	private static final JavaField CHARACTER_MIN_VALUE = new JavaField(JavaClass.CHARACTER, "MIN_VALUE", "C");
	private static final JavaField CHARACTER_MAX_VALUE = new JavaField(JavaClass.CHARACTER, "MAX_VALUE", "C");
	private static final JavaNativeMethod CHARACTER_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.CHARACTER, "toString", "(C)Ljava/lang/String;");
	private static final JavaNativeMethod STRING_INIT_CHARACTERS = JavaNativeMethod.getNativeConstructor(JavaClass.STRING, "([C)V");
	private static final JavaNativeMethod STRING_INIT_BYTES_CHARSET = JavaNativeMethod.getNativeConstructor(JavaClass.STRING, "([BLjava/nio/charset/Charset;)V");
	private static final JavaNativeMethod STRING_COMPARETO = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "compareTo", "(Ljava/lang/String;)I");
	private static final JavaNativeMethod STRING_CONCAT = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "concat", "(Ljava/lang/String;)Ljava/lang/String;");
	private static final JavaNativeMethod STRING_CHAR_AT = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "charAt", "(I)C");
	private static final JavaNativeMethod STRING_SUBSTRING = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "substring", "(II)Ljava/lang/String;");
	private static final JavaNativeMethod STRING_TRIM = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "trim", "()Ljava/lang/String;");
	private static final JavaNativeMethod STRING_TO_LOWER_CASE = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "toLowerCase", "()Ljava/lang/String;");
	private static final JavaNativeMethod STRING_TO_UPPER_CASE = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "toUpperCase", "()Ljava/lang/String;");
	private static final JavaNativeMethod STRING_LENGTH = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "length", "()I");
	private static final JavaNativeMethod STRING_CHARACTERS = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "toCharArray", "()[C");
	private static final JavaNativeMethod STRING_ISEMPTY = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "isEmpty", "()Z");
	private static final JavaNativeMethod STRING_GET_BYTES = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "getBytes", "(Ljava/nio/charset/Charset;)[B");
	private static final JavaNativeMethod ENUM_COMPARETO = JavaNativeMethod.getNativeVirtual(JavaClass.ENUM, "compareTo", "(Ljava/lang/Enum;)I");
	private static final JavaNativeMethod ENUM_NAME = JavaNativeMethod.getNativeVirtual(JavaClass.ENUM, "name", "()Ljava/lang/String;");
	private static final JavaNativeMethod ENUM_ORDINAL = JavaNativeMethod.getNativeVirtual(JavaClass.ENUM, "ordinal", "()I");
	private static final JavaNativeMethod HASHMAP_INIT = JavaNativeMethod.getNativeConstructor(JavaClass.HASHMAP, "()V");
	private static final JavaNativeMethod MAP_GET = JavaNativeMethod.getInterface(JavaClass.MAP, "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
	private static final JavaNativeMethod MAP_PUT = JavaNativeMethod.getInterface(JavaClass.MAP, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
	private static final JavaNativeMethod MAP_PUT_ALL = JavaNativeMethod.getInterface(JavaClass.MAP, "putAll", "(Ljava/util/Map;)V");
	private static final JavaNativeMethod MAP_CONTAINS_KEY = JavaNativeMethod.getInterface(JavaClass.MAP, "containsKey", "(Ljava/lang/Object;)Z");
	private static final JavaNativeMethod MAP_SIZE = JavaNativeMethod.getInterface(JavaClass.MAP, "size", "()I");
	private static final JavaNativeMethod MAP_ISEMPTY = JavaNativeMethod.getInterface(JavaClass.MAP, "isEmpty", "()Z");
	private static final JavaNativeMethod MAP_KEYS = JavaNativeMethod.getInterface(JavaClass.MAP, "keys", "()Ljava/lang/Object;");
	private static final JavaNativeMethod MAP_VALUES = JavaNativeMethod.getInterface(JavaClass.MAP, "values", "()Ljava/lang/Object;");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_OBJECTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([Ljava/lang/Object;II)[Ljava/lang/Object;");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_BOOLS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([ZII)[Z");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_BYTES = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([BII)[B");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_SHORTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([SII)[S");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_INTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([III)[I");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_LONGS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([JII)[J");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_FLOATS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([FII)[F");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_DOUBLES = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([DII)[D");
	private static final JavaNativeMethod ARRAYS_COPY_OF_RANGE_CHARS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([CII)[C");
	private static final JavaNativeMethod ARRAYS_EQUALS_OBJECTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([Ljava/lang/Object[Ljava/lang/Object)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_BOOLS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([Z[Z)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_BYTES = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([B[B)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_SHORTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([S[S)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_INTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([I[I)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_LONGS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([L[L)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_FLOATS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([F[F)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_DOUBLES = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([D[D)Z");
	private static final JavaNativeMethod ARRAYS_EQUALS_CHARS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([C[C)Z");
	private static final JavaNativeMethod ARRAYS_DEEPHASHCODE = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "deepHashCode", "([Ljava/lang/Object;)");
	private static final JavaNativeMethod ARRAYS_HASHCODE_BOOLS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([Z)I");
	private static final JavaNativeMethod ARRAYS_HASHCODE_BYTES = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([B)I");
	private static final JavaNativeMethod ARRAYS_HASHCODE_SHORTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([S)I");
	private static final JavaNativeMethod ARRAYS_HASHCODE_INTS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([I)I");
	private static final JavaNativeMethod ARRAYS_HASHCODE_LONGS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([L)I");
	private static final JavaNativeMethod ARRAYS_HASHCODE_FLOATS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([F)I");
	private static final JavaNativeMethod ARRAYS_HASHCODE_DOUBLES = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([D)I");
	private static final JavaNativeMethod ARRAYS_HASHCODE_CHARS = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([C)I");
	private static final JavaNativeMethod COLLECTION_SIZE = JavaNativeMethod.getNativeVirtual(JavaClass.COLLECTION, "size", "()I");
	private static final JavaNativeMethod COLLECTION_TOARRAY = JavaNativeMethod.getNativeVirtual(JavaClass.COLLECTION, "toArray", "([Ljava/lang/Object;)[Ljava/lang/Object;");

	private final JavaWriter javaWriter;
	private final JavaExpressionVisitor expressionVisitor;
	private final JavaBoxingTypeVisitor boxingTypeVisitor;
	private final JavaUnboxingTypeVisitor unboxingTypeVisitor;
	private final JavaBytecodeContext context;

	public JavaMethodBytecodeCompiler(JavaWriter javaWriter, JavaExpressionVisitor expressionVisitor, JavaBytecodeContext context) {
		this.javaWriter = javaWriter;
		this.expressionVisitor = expressionVisitor;
		boxingTypeVisitor = new JavaBoxingTypeVisitor(javaWriter);
		unboxingTypeVisitor = new JavaUnboxingTypeVisitor(javaWriter);
		this.context = context;
	}

	@Override
	public Void nativeMethod(JavaNativeMethod method, TypeID returnType, CallArguments arguments) {
		for (Expression argument : arguments.arguments) {
			argument.accept(expressionVisitor);
		}
		if (method.compile) {
			handleTypeArguments(method, arguments);
		}

		if (method.kind == JavaNativeMethod.Kind.STATIC) {
			javaWriter.invokeStatic(method);
		} else if (method.kind == JavaNativeMethod.Kind.INTERFACE) {
			javaWriter.invokeInterface(method);
		} else if (method.kind == JavaNativeMethod.Kind.EXPANSION) {
			javaWriter.invokeStatic(method);
		} else if (method.cls != null && method.cls.kind == JavaClass.Kind.INTERFACE) {
			javaWriter.invokeInterface(method);
		} else {
			javaWriter.invokeVirtual(method);
		}
		if (method.genericResult) {
			handleGenericReturnValue(returnType);
		}

		//Make sure that method results are popped if ZC thinks its a void but it actually is not.
		//Fixes an issue for List#add() returning void in ZC but Z in Java.
		if (returnType == BasicTypeID.VOID && !method.descriptor.equals("") && !method.descriptor.endsWith(")V")) {
			final boolean isLarge = method.descriptor.endsWith(")D") && method.descriptor.endsWith(")J");
			javaWriter.pop(isLarge);
		}

		return null;
	}

	private void handleTypeArguments(JavaNativeMethod method, CallArguments arguments) {
		final JavaTypeExpressionVisitor javaTypeExpressionVisitor = new JavaTypeExpressionVisitor(context);
		if (arguments.typeArguments.length != method.typeParameterArguments.length)
			throw new IllegalArgumentException("Number of type parameters doesn't match");

		for (int i = 0; i < arguments.typeArguments.length; i++) {
			if (method.typeParameterArguments[i])
				arguments.typeArguments[i].accept(javaWriter, javaTypeExpressionVisitor);
		}
	}

	private void handleGenericReturnValue(TypeID actual) {
		if (CompilerUtils.isPrimitive(actual)) {
			javaWriter.checkCast(context.getInternalName(new OptionalTypeID(actual)));
			actual.accept(actual, unboxingTypeVisitor);
		} else {
			javaWriter.checkCast(context.getInternalName(actual));
		}
	}

	@Override
	public Void builtinMethod(BuiltinMethodSymbol method, CallArguments args) {
		Expression[] arguments = args.arguments;
		switch (method) {
			case STRING_RANGEGET: {
				arguments[0].accept(expressionVisitor);
				Expression argument = arguments[1];
				if (argument instanceof RangeExpression) {
					RangeExpression rangeArgument = (RangeExpression) argument;
					rangeArgument.from.accept(expressionVisitor);
					rangeArgument.to.accept(expressionVisitor); // TODO: is this string.length ? if so, use the other substring method
				} else {
					argument.accept(expressionVisitor);
					javaWriter.dup();
					final String owner;
					if (argument.type instanceof RangeTypeID) {
						owner = context.getInternalName(argument.type);
					} else {
						owner = "zsynthetic/IntRange";
					}
					int tmp = javaWriter.local(Type.getType(owner));
					javaWriter.storeInt(tmp);
					javaWriter.getField(owner, "from", "I");
					javaWriter.loadInt(tmp);
					javaWriter.getField(owner, "to", "I");
				}
				javaWriter.invokeVirtual(STRING_SUBSTRING);
				return null;
			}
			case ARRAY_CONTAINS: {
				arguments[0].accept(expressionVisitor);
				final Label loopStart = new Label();
				final Label loopEnd = new Label();
				final Label isTrue = new Label();
				final Label expressionEnd = new Label();

				final int counterLocation = javaWriter.local(int.class);
				javaWriter.iConst0();
				javaWriter.storeInt(counterLocation);

				javaWriter.label(loopStart);
				javaWriter.dup();
				javaWriter.arrayLength();

				javaWriter.loadInt(counterLocation);

				javaWriter.ifICmpLE(loopEnd);
				javaWriter.dup();
				javaWriter.loadInt(counterLocation);
				final TypeID itemType = arguments[1].type;
				javaWriter.arrayLoad(context.getType(itemType));
				javaWriter.iinc(counterLocation);
				arguments[1].accept(expressionVisitor);

				if (CompilerUtils.isPrimitive(itemType)) {
					//Compare non-int types beforehand
					if (itemType == BasicTypeID.LONG || itemType == BasicTypeID.ULONG) {
						javaWriter.lCmp();
						javaWriter.ifEQ(loopStart);
					} else if (itemType == BasicTypeID.FLOAT) {
						javaWriter.fCmp();
						javaWriter.ifEQ(loopStart);
					} else if (itemType == BasicTypeID.DOUBLE) {
						javaWriter.dCmp();
						javaWriter.ifEQ(loopStart);
					} else
						javaWriter.ifICmpNE(loopStart);
				} else {
					//If equals, use Object.equals in case of null
					javaWriter.invokeStatic(new JavaNativeMethod(JavaClass.fromInternalName("java/util/Objects", JavaClass.Kind.CLASS), JavaNativeMethod.Kind.STATIC, "equals", false, "(Ljava/lang/Object;Ljava/lang/Object;)Z", 0, false));
					javaWriter.ifEQ(loopStart);
					// If ==
					// javaWriter.ifACmpNe(loopStart);
				}

				javaWriter.label(isTrue);

				javaWriter.pop();
				javaWriter.iConst1();
				javaWriter.goTo(expressionEnd);

				javaWriter.label(loopEnd);
				javaWriter.pop();
				javaWriter.iConst0();
				javaWriter.label(expressionEnd);

				return null;
			}
			case ARRAY_EQUALS:
			case ARRAY_NOTEQUALS: {
				ArrayTypeID type = arguments[0].type.asArray()
						.orElseThrow(() -> new IllegalStateException("Illegal target type: " + arguments[0].type));

				if (type.elementType instanceof BasicTypeID) {
					switch ((BasicTypeID) type.elementType) {
						case BOOL:
							javaWriter.invokeStatic(ARRAYS_EQUALS_BOOLS);
							break;
						case BYTE:
						case SBYTE:
							javaWriter.invokeStatic(ARRAYS_EQUALS_BYTES);
							break;
						case SHORT:
						case USHORT:
							javaWriter.invokeStatic(ARRAYS_EQUALS_SHORTS);
							break;
						case INT:
						case UINT:
							javaWriter.invokeStatic(ARRAYS_EQUALS_INTS);
							break;
						case LONG:
						case ULONG:
							javaWriter.invokeStatic(ARRAYS_EQUALS_LONGS);
							break;
						case FLOAT:
							javaWriter.invokeStatic(ARRAYS_EQUALS_FLOATS);
							break;
						case DOUBLE:
							javaWriter.invokeStatic(ARRAYS_EQUALS_DOUBLES);
							break;
						case CHAR:
							javaWriter.invokeStatic(ARRAYS_EQUALS_CHARS);
							break;
						default:
							throw new IllegalArgumentException("Unknown basic type: " + type.elementType);
					}
				} else {
					javaWriter.invokeStatic(ARRAYS_EQUALS_OBJECTS);
				}

				if (method == BuiltinMethodSymbol.ARRAY_NOTEQUALS) {
					javaWriter.invertBoolean();
				}
				break;
			}
		}

		for (Expression argument : arguments) {
			argument.accept(expressionVisitor);
		}

		switch (method) {
			case BOOL_AND:
				javaWriter.iAnd();
				break;
			case BOOL_OR:
				javaWriter.iOr();
				break;
			case BOOL_XOR:
				javaWriter.iXor();
				break;
			case BOOL_EQUALS:
				javaWriter.iXor();
				javaWriter.iConst1();
				javaWriter.iXor();
				break;
			case BOOL_NOTEQUALS:
				javaWriter.iXor();
				break;
			case BYTE_ADD_BYTE:
			case SBYTE_ADD_SBYTE:
			case SHORT_ADD_SHORT:
			case USHORT_ADD_USHORT:
			case INT_ADD_INT:
			case UINT_ADD_UINT:
			case USIZE_ADD_USIZE:
				javaWriter.iAdd();
				break;
			case BYTE_SUB_BYTE:
			case SBYTE_SUB_SBYTE:
			case SHORT_SUB_SHORT:
			case USHORT_SUB_USHORT:
			case INT_SUB_INT:
			case UINT_SUB_UINT:
			case USIZE_SUB_USIZE:
				javaWriter.iSub();
				break;
			case BYTE_MUL_BYTE:
			case SBYTE_MUL_SBYTE:
			case SHORT_MUL_SHORT:
			case USHORT_MUL_USHORT:
			case INT_MUL_INT:
			case UINT_MUL_UINT:
			case USIZE_MUL_USIZE:
				javaWriter.iMul();
				break;
			case SBYTE_DIV_SBYTE:
			case SHORT_DIV_SHORT:
			case INT_DIV_INT:
			case USIZE_DIV_USIZE:
				javaWriter.iDiv();
				break;
			case SBYTE_MOD_SBYTE:
			case SHORT_MOD_SHORT:
			case INT_MOD_INT:
			case USIZE_MOD_USIZE:
				javaWriter.iRem();
				break;
			case BYTE_AND_BYTE:
			case SBYTE_AND_SBYTE:
			case SHORT_AND_SHORT:
			case USHORT_AND_USHORT:
			case INT_AND_INT:
			case UINT_AND_UINT:
			case USIZE_AND_USIZE:
				javaWriter.iAnd();
				break;
			case BYTE_OR_BYTE:
			case SBYTE_OR_SBYTE:
			case SHORT_OR_SHORT:
			case USHORT_OR_USHORT:
			case INT_OR_INT:
			case UINT_OR_UINT:
			case USIZE_OR_USIZE:
				javaWriter.iOr();
				break;
			case BYTE_XOR_BYTE:
			case SBYTE_XOR_SBYTE:
			case SHORT_XOR_SHORT:
			case USHORT_XOR_USHORT:
			case INT_XOR_INT:
			case UINT_XOR_UINT:
			case USIZE_XOR_USIZE:
				javaWriter.iXor();
				break;
			case BYTE_SHL:
			case SBYTE_SHL:
			case SHORT_SHL:
			case USHORT_SHL:
			case INT_SHL:
			case UINT_SHL:
			case USIZE_SHL:
				javaWriter.iShl();
				break;
			case SBYTE_SHR:
			case SHORT_SHR:
			case INT_SHR:
				javaWriter.iShr();
				break;
			case BYTE_SHR:
			case USHORT_SHR:
			case UINT_SHR:
			case USIZE_SHR:
				javaWriter.iUShr();
				break;
			case LONG_ADD_LONG:
			case ULONG_ADD_ULONG:
				javaWriter.lAdd();
				break;
			case LONG_SUB_LONG:
			case ULONG_SUB_ULONG:
				javaWriter.lSub();
				break;
			case LONG_MUL_LONG:
			case ULONG_MUL_ULONG:
				javaWriter.lMul();
				break;
			case LONG_DIV_LONG:
				javaWriter.lDiv();
				break;
			case LONG_MOD_LONG:
				javaWriter.lRem();
				break;
			case LONG_AND_LONG:
			case ULONG_AND_ULONG:
				javaWriter.lAnd();
				break;
			case LONG_OR_LONG:
			case ULONG_OR_ULONG:
				javaWriter.lOr();
				break;
			case LONG_XOR_LONG:
			case ULONG_XOR_ULONG:
				javaWriter.lXor();
				break;
			case LONG_SHL:
			case ULONG_SHL:
				javaWriter.lShl();
				break;
			case LONG_SHR:
				javaWriter.lShr();
				break;
			case ULONG_SHR:
				javaWriter.lUShr();
				break;
			case ULONG_DIV_ULONG:
				javaWriter.invokeStatic(LONG_DIVIDE_UNSIGNED);
				break;
			case ULONG_MOD_ULONG:
				javaWriter.invokeStatic(LONG_REMAINDER_UNSIGNED);
				break;
			case FLOAT_ADD_FLOAT:
				javaWriter.fAdd();
				break;
			case FLOAT_SUB_FLOAT:
				javaWriter.fSub();
				break;
			case FLOAT_MUL_FLOAT:
				javaWriter.fMul();
				break;
			case FLOAT_DIV_FLOAT:
				javaWriter.fDiv();
				break;
			case FLOAT_MOD_FLOAT:
				javaWriter.fRem();
				break;
			case DOUBLE_ADD_DOUBLE:
				javaWriter.dAdd();
				break;
			case DOUBLE_SUB_DOUBLE:
				javaWriter.dSub();
				break;
			case DOUBLE_MUL_DOUBLE:
				javaWriter.dMul();
				break;
			case DOUBLE_DIV_DOUBLE:
				javaWriter.dDiv();
				break;
			case DOUBLE_MOD_DOUBLE:
				javaWriter.dRem();
				break;
			case STRING_ADD_STRING:
				javaWriter.invokeVirtual(STRING_CONCAT);
				break;
			case STRING_INDEXGET:
				javaWriter.invokeVirtual(STRING_CHAR_AT);
				break;
			case ASSOC_CONTAINS:
				javaWriter.invokeVirtual(MAP_CONTAINS_KEY);
				break;
			case ASSOC_INDEXGET: {
				AssocTypeID type = (AssocTypeID) arguments[0].type;
				type.keyType.accept(type.keyType, boxingTypeVisitor);
				javaWriter.invokeInterface(MAP_GET);

				type.valueType.accept(type.valueType, unboxingTypeVisitor);
				if (!CompilerUtils.isPrimitive(type.valueType)) {
					javaWriter.checkCast(context.getType(type.valueType));
				}
				break;
			}
			case OBJECT_SAME:
			case ASSOC_SAME:
			case ARRAY_SAME: {
				Label exit = new Label();
				javaWriter.iConst0();
				javaWriter.ifACmpNe(exit);
				javaWriter.iConst1();
				javaWriter.label(exit);
				break;
			}
			case OBJECT_NOTSAME:
			case ASSOC_NOTSAME:
			case ARRAY_NOTSAME: {
				Label exit = new Label();
				javaWriter.iConst0();
				javaWriter.ifACmpEq(exit);
				javaWriter.iConst1();
				javaWriter.label(exit);
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown builtin: " + method);
		}
		return null;
	}
}
