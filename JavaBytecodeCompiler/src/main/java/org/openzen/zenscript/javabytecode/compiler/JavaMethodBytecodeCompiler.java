package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javashared.*;

import java.util.concurrent.atomic.AtomicInteger;

public class JavaMethodBytecodeCompiler implements JavaMethodCompiler<Void> {
	private static final JavaNativeMethod BOOLEAN_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.BOOLEAN, "toString", "(Z)Ljava/long/String;");

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
	private static final JavaNativeField INTEGER_MIN_VALUE = new JavaNativeField(JavaClass.INTEGER, "MIN_VALUE", "I");
	private static final JavaNativeField INTEGER_MAX_VALUE = new JavaNativeField(JavaClass.INTEGER, "MAX_VALUE", "I");
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
	private static final JavaNativeField LONG_MIN_VALUE = new JavaNativeField(JavaClass.LONG, "MIN_VALUE", "J");
	private static final JavaNativeField LONG_MAX_VALUE = new JavaNativeField(JavaClass.LONG, "MAX_VALUE", "J");
	private static final JavaNativeMethod LONG_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "toString", "(J)Ljava/lang/String;");
	private static final JavaNativeMethod LONG_TO_UNSIGNED_STRING = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "toUnsignedString", "(J)Ljava/lang/String;");
	private static final JavaNativeMethod FLOAT_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "toString", "(F)Ljava/lang/String;");
	private static final JavaNativeMethod DOUBLE_TO_STRING = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "toString", "(D)Ljava/lang/String;");

	private static final JavaNativeMethod CHARACTER_TO_LOWER_CASE = JavaNativeMethod.getNativeVirtual(JavaClass.CHARACTER, "toLowerCase", "()C");
	private static final JavaNativeMethod CHARACTER_TO_UPPER_CASE = JavaNativeMethod.getNativeVirtual(JavaClass.CHARACTER, "toUpperCase", "()C");
	private static final JavaNativeField CHARACTER_MIN_VALUE = new JavaNativeField(JavaClass.CHARACTER, "MIN_VALUE", "C");
	private static final JavaNativeField CHARACTER_MAX_VALUE = new JavaNativeField(JavaClass.CHARACTER, "MAX_VALUE", "C");
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
	private static final JavaNativeMethod STRING_CONTAINS = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "contains", "(Ljava/lang/CharSequence;)Z");
	private static final JavaNativeMethod ENUM_COMPARETO = JavaNativeMethod.getNativeVirtual(JavaClass.ENUM, "compareTo", "(Ljava/lang/Enum;)I");
	private static final JavaNativeMethod HASHMAP_INIT = JavaNativeMethod.getNativeConstructor(JavaClass.HASHMAP, "()V");
	private static final JavaNativeMethod MAP_GET = JavaNativeMethod.getInterface(JavaClass.MAP, "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
	private static final JavaNativeMethod MAP_PUT = JavaNativeMethod.getInterface(JavaClass.MAP, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
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

	private static final JavaNativeMethod OBJECTS_EQUALS = new JavaNativeMethod(JavaClass.fromInternalName("java/util/Objects", JavaClass.Kind.CLASS), JavaNativeMethod.Kind.STATIC, "equals", false, "(Ljava/lang/Object;Ljava/lang/Object;)Z", 0, false);

	private static final JavaNativeMethod STRINGBUILDER_LENGTH = JavaNativeMethod.getNativeVirtual(JavaClass.STRINGBUILDER, "length", "()I");

	private final JavaWriter javaWriter;
	private final JavaExpressionVisitor expressionVisitor;
	private final JavaBoxingTypeVisitor boxingTypeVisitor;
	private final JavaUnboxingTypeVisitor unboxingTypeVisitor;
	private final JavaBytecodeContext context;
	private final JavaCompiledModule module;

	public JavaMethodBytecodeCompiler(JavaWriter javaWriter, JavaExpressionVisitor expressionVisitor, JavaBytecodeContext context, JavaCompiledModule module) {
		this.javaWriter = javaWriter;
		this.expressionVisitor = expressionVisitor;
		boxingTypeVisitor = new JavaBoxingTypeVisitor(javaWriter);
		unboxingTypeVisitor = new JavaUnboxingTypeVisitor(javaWriter);
		this.context = context;
		this.module = module;
	}

	@Override
	public Void nativeConstructor(JavaNativeMethod method, TypeID type, CallArguments arguments) {
		javaWriter.newObject(method.cls);
		javaWriter.dup();
		AtomicInteger typeArguments = new AtomicInteger(0);
		if (method.compile) {
			type.asDefinition().ifPresent(definitionType -> {
				final JavaTypeExpressionVisitor javaTypeExpressionVisitor = new JavaTypeExpressionVisitor(context);
				typeArguments.set(definitionType.typeArguments.length);
				for (TypeID typeParameter : definitionType.typeArguments) {
					typeParameter.accept(javaWriter, javaTypeExpressionVisitor);
				}
			});
		}
		handleArguments(typeArguments.get(), method, arguments);
		javaWriter.invokeSpecial(method);
		return null;
	}

	@Override
	public Void nativeVirtualMethod(JavaNativeMethod method, TypeID returnType, Expression target, CallArguments arguments) {
		if (arguments.expansionTypeArguments.length > 0) {
			final JavaTypeExpressionVisitor javaTypeExpressionVisitor = new JavaTypeExpressionVisitor(context);
			for (int i = 0; i < arguments.expansionTypeArguments.length; i++) {
				arguments.expansionTypeArguments[i].accept(javaWriter, javaTypeExpressionVisitor);
			}
		}
		target.accept(expressionVisitor);
		return nativeStaticMethod(method, returnType, arguments);
	}

	@Override
	public Void nativeStaticMethod(JavaNativeMethod method, TypeID returnType, CallArguments arguments) {
		handleArguments(arguments.typeArguments.length, method, arguments);

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
		if (returnType == BasicTypeID.VOID && !method.descriptor.isEmpty() && !method.descriptor.endsWith(")V")) {
			final boolean isLarge = method.descriptor.endsWith(")D") || method.descriptor.endsWith(")J");
			javaWriter.pop(isLarge);
		}

		return null;
	}

	@Override
	public Void nativeSpecialMethod(JavaNativeMethod method, TypeID returnType, Expression target, CallArguments arguments) {
		target.accept(expressionVisitor);
		handleArguments(arguments.typeArguments.length, method, arguments);
		javaWriter.invokeSpecial(method);
		return null;
	}

	private void handleArguments(int typeArguments, JavaNativeMethod method, CallArguments arguments) {
		if (method.compile) {
			handleTypeArguments(method, arguments);
		}

		// This happens e.g. for Strings where compareTo is a static method in zencode but a virtual one in Java
		boolean[] primitiveArguments = method.primitiveArguments;
		if (primitiveArguments.length + 1 == arguments.arguments.length) {
			primitiveArguments = new boolean[arguments.arguments.length];
			primitiveArguments[0] = false; // Let's just assume they are all for object types
			System.arraycopy(method.primitiveArguments, 0, primitiveArguments, 1, method.primitiveArguments.length);
		}

		for (int index = 0; index < arguments.arguments.length; index++) {
			Expression argument = arguments.arguments[index];
			argument.accept(expressionVisitor);
			if (!primitiveArguments[typeArguments + index + (method.cls.kind == JavaClass.Kind.EXPANSION ? 1 : 0)]) {
				argument.type.accept(argument.type, boxingTypeVisitor);
			}
		}
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
	public Void builtinConstructor(BuiltinMethodSymbol method, TypeID type, CallArguments args) {
		Expression[] arguments = args.arguments;
		switch (method) {
			case STRING_CONSTRUCTOR_CHARACTERS:
				javaWriter.newObject(JavaClass.STRING);
				javaWriter.dup();
				arguments[0].accept(expressionVisitor);
				javaWriter.invokeSpecial(STRING_INIT_CHARACTERS);
				return null;
			case ASSOC_CONSTRUCTOR:
			case GENERICMAP_CONSTRUCTOR: {
				javaWriter.newObject(JavaClass.HASHMAP);
				javaWriter.dup();
				javaWriter.invokeSpecial(HASHMAP_INIT);
				return null;
			}
			case ARRAY_CONSTRUCTOR_SIZED:
			case ARRAY_CONSTRUCTOR_INITIAL_VALUE: {
				ArrayTypeID arrayType = (ArrayTypeID) type;

				final Type ASMElementType = context.getType(arrayType.elementType);

				final Label begin = new Label();
				final Label end = new Label();

				javaWriter.label(begin);
				final int defaultValueLocation = javaWriter.local(ASMElementType);
				javaWriter.addVariableInfo(new JavaLocalVariableInfo(ASMElementType, defaultValueLocation, begin, "defaultValue", end));


				if (method == BuiltinMethodSymbol.ARRAY_CONSTRUCTOR_SIZED) {
					arrayType.elementType.getDefaultValue().accept(expressionVisitor);
				} else {
					arguments[arguments.length - 1].accept(expressionVisitor);
				}
				javaWriter.store(ASMElementType, defaultValueLocation);


				final int[] arraySizes = ArrayInitializerHelper.getArraySizeLocationsFromConstructor(arrayType.dimension, arguments, expressionVisitor);
				new ArrayInitializerHelper(context).visitMultiDimArrayWithDefaultValue(javaWriter, arraySizes, arrayType.dimension, new ArrayHelperType(arrayType, context), arrayType, defaultValueLocation);

				javaWriter.label(end);
				return null;
			}
			case ARRAY_CONSTRUCTOR_LAMBDA: {
				ArrayTypeID arrayType = (ArrayTypeID) type;

				//Labels
				final Label begin = new Label();
				final Label end = new Label();
				javaWriter.label(begin);

				final int dimension = ((ArrayTypeID) type).dimension;
				final int[] arraySizes = ArrayInitializerHelper.getArraySizeLocationsFromConstructor(dimension, arguments, expressionVisitor);
				new ArrayInitializerHelper(context).visitMultiDimArray(javaWriter, arraySizes, new int[dimension], dimension, new ArrayHelperType(arrayType, context), arrayType, (elementType, counterLocations) -> {
					arguments[dimension].accept(expressionVisitor);
					for (int counterLocation : counterLocations) {
						javaWriter.loadInt(counterLocation);
					}
					javaWriter.invokeInterface(context.getFunctionalInterface(arguments[dimension].type));
				});
				javaWriter.label(end);
				return null;
			}
		}
		return null;
	}

	@Override
	public Void builtinVirtualMethod(BuiltinMethodSymbol method, Expression target, CallArguments arguments) {
		return builtinStaticMethod(method, target.type, arguments.bind(target));
	}

	@Override
	public Void builtinStaticMethod(BuiltinMethodSymbol method, TypeID returnType, CallArguments args) {
		Expression[] arguments = args.arguments;
		switch (method) {
			case BOOL_NOT:
				arguments[0].accept(expressionVisitor);
				javaWriter.invertBoolean();
				return null;

			case BOOL_ADD_STRING:
			case BOOL_CAT_STRING:
				arguments[0].accept(expressionVisitor);
				javaWriter.invokeStatic(BOOLEAN_TO_STRING);
				arguments[1].accept(expressionVisitor);
				javaWriter.invokeVirtual(STRING_CONCAT);
				return null;
			case BYTE_ADD_STRING:
			case BYTE_CAT_STRING:
				arguments[0].accept(expressionVisitor);
				javaWriter.constant(0xFF);
				javaWriter.iAnd();
				javaWriter.invokeStatic(INTEGER_TO_STRING);
				arguments[1].accept(expressionVisitor);
				javaWriter.invokeVirtual(STRING_CONCAT);
				return null;
			case SBYTE_ADD_STRING:
			case SBYTE_CAT_STRING:
				arguments[0].accept(expressionVisitor);
				javaWriter.invokeStatic(BYTE_TO_STRING);
				arguments[1].accept(expressionVisitor);
				javaWriter.invokeVirtual(STRING_CONCAT);
				return null;
			case SHORT_ADD_STRING:
			case SHORT_CAT_STRING:
				arguments[0].accept(expressionVisitor);
				javaWriter.invokeStatic(SHORT_TO_STRING);
				arguments[1].accept(expressionVisitor);
				javaWriter.invokeVirtual(STRING_CONCAT);
				return null;
			case USHORT_ADD_STRING:
			case USHORT_CAT_STRING:
				arguments[0].accept(expressionVisitor);
				javaWriter.constant(0xFFFF);
				javaWriter.iAnd();
				javaWriter.invokeStatic(INTEGER_TO_STRING);
				arguments[1].accept(expressionVisitor);
				javaWriter.invokeVirtual(STRING_CONCAT);
				return null;
			case INT_ADD_STRING:
			case INT_CAT_STRING:
				arguments[0].accept(expressionVisitor);
				javaWriter.invokeStatic(INTEGER_TO_STRING);
				arguments[1].accept(expressionVisitor);
				javaWriter.invokeVirtual(STRING_CONCAT);
				return null;
			case UINT_ADD_STRING:
			case UINT_CAT_STRING:
			case USIZE_ADD_STRING:
			case USIZE_CAT_STRING:
				arguments[0].accept(expressionVisitor);
				javaWriter.invokeStatic(INTEGER_TO_UNSIGNED_STRING);
				arguments[1].accept(expressionVisitor);
				javaWriter.invokeVirtual(STRING_CONCAT);
				return null;
			case LONG_ADD_STRING:
			case LONG_CAT_STRING:
				arguments[0].accept(expressionVisitor);
				javaWriter.invokeStatic(LONG_TO_STRING);
				arguments[1].accept(expressionVisitor);
				javaWriter.invokeVirtual(STRING_CONCAT);
				return null;
			case ULONG_ADD_STRING:
			case ULONG_CAT_STRING:
				arguments[0].accept(expressionVisitor);
				javaWriter.invokeStatic(LONG_TO_UNSIGNED_STRING);
				arguments[1].accept(expressionVisitor);
				javaWriter.invokeVirtual(STRING_CONCAT);
				return null;
			case FLOAT_ADD_STRING:
			case FLOAT_CAT_STRING:
				arguments[0].accept(expressionVisitor);
				javaWriter.invokeStatic(FLOAT_TO_STRING);
				arguments[1].accept(expressionVisitor);
				javaWriter.invokeVirtual(STRING_CONCAT);
				return null;
			case DOUBLE_ADD_STRING:
			case DOUBLE_CAT_STRING:
				arguments[0].accept(expressionVisitor);
				javaWriter.invokeStatic(DOUBLE_TO_STRING);
				arguments[1].accept(expressionVisitor);
				javaWriter.invokeVirtual(STRING_CONCAT);
				return null;
			case STRING_CONTAINS_STRING:
			case STRING_CONTAINS_CHAR: {
				arguments[0].accept(expressionVisitor);
				arguments[1].accept(expressionVisitor);
				if (method == BuiltinMethodSymbol.STRING_CONTAINS_CHAR) {
					javaWriter.invokeStatic(CHARACTER_TO_STRING);
				}

				javaWriter.invokeVirtual(STRING_CONTAINS);
				return null;
			}
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
					javaWriter.invokeStatic(OBJECTS_EQUALS);
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
			case ASSOC_INDEXSET: {
				AssocTypeID type = (AssocTypeID) arguments[0].type;

				arguments[0].accept(expressionVisitor);
				arguments[1].accept(expressionVisitor);
				type.keyType.accept(type.keyType, boxingTypeVisitor);
				arguments[2].accept(expressionVisitor);
				type.valueType.accept(type.valueType, boxingTypeVisitor);
				javaWriter.invokeInterface(MAP_PUT);
				javaWriter.pop();
				return null;
			}
		}

		for (Expression argument : arguments) {
			argument.accept(expressionVisitor);
		}

		switch (method) {
			case FUNCTION_CALL: // ToDo: Was added from dev->refactor branch during rebase, is this still needed?
				FunctionTypeID expressionType = (FunctionTypeID) arguments[0].type;
				JavaNativeMethod functionalInterface = context.getFunctionalInterface(expressionType);
				javaWriter.invokeInterface(functionalInterface);
				expressionVisitor.handleReturnValue(context.getFunction(expressionType).getHeader().getReturnType(), method.getHeader().instanceForCall(args).getReturnType());
				break;

			/* Casts */
			case BOOL_TO_STRING:
				if (arguments[0].type.isOptional()) {
					javaWriter.invokeStatic(OBJECTS_TOSTRING);
				} else {
					javaWriter.invokeStatic(BOOLEAN_TO_STRING);
				}
				break;
			case BYTE_TO_SBYTE:
				javaWriter.i2b();
				break;
			case BYTE_TO_SHORT:
				javaWriter.constant(0xFF);
				javaWriter.iAnd();
				javaWriter.i2s();
				break;
			case BYTE_TO_USHORT:
			case BYTE_TO_INT:
			case BYTE_TO_UINT:
			case BYTE_TO_USIZE:
				javaWriter.constant(0xFF);
				javaWriter.iAnd();
				break;
			case BYTE_TO_LONG:
			case BYTE_TO_ULONG:
				javaWriter.constant(0xFF);
				javaWriter.iAnd();
				javaWriter.i2l();
				break;
			case BYTE_TO_FLOAT:
				javaWriter.constant(0xFF);
				javaWriter.iAnd();
				javaWriter.i2f();
				break;
			case BYTE_TO_DOUBLE:
				javaWriter.constant(0xFF);
				javaWriter.iAnd();
				javaWriter.i2d();
				break;
			case BYTE_TO_CHAR:
				javaWriter.constant(0xFF);
				javaWriter.iAnd();
				break;
			case BYTE_TO_STRING:
				if (arguments[0].type.isOptional()) {
					javaWriter.invokeStatic(OBJECTS_TOSTRING);
				} else {
					javaWriter.constant(0xFF);
					javaWriter.iAnd();
					javaWriter.invokeStatic(INTEGER_TO_STRING);
				}
				break;
			case SBYTE_TO_BYTE:
			case SBYTE_TO_SHORT:
			case SBYTE_TO_USHORT:
			case SBYTE_TO_INT:
			case SBYTE_TO_UINT:
			case SBYTE_TO_USIZE:
				break;
			case SBYTE_TO_LONG:
			case SBYTE_TO_ULONG:
				javaWriter.i2l();
				break;
			case SBYTE_TO_FLOAT:
				javaWriter.i2f();
				break;
			case SBYTE_TO_DOUBLE:
				javaWriter.i2d();
				break;
			case SBYTE_TO_CHAR:
				break;
			case SBYTE_TO_STRING:
				if (arguments[0].type.isOptional()) {
					javaWriter.invokeStatic(OBJECTS_TOSTRING);
				} else {
					javaWriter.invokeStatic(INTEGER_TO_STRING);
				}
				break;
			case SHORT_TO_BYTE:
				break;
			case SHORT_TO_SBYTE:
				javaWriter.i2b();
				break;
			case SHORT_TO_USHORT:
			case SHORT_TO_INT:
			case SHORT_TO_UINT:
			case SHORT_TO_USIZE:
				break;
			case SHORT_TO_LONG:
			case SHORT_TO_ULONG:
				javaWriter.i2l();
				break;
			case SHORT_TO_FLOAT:
				javaWriter.i2f();
				break;
			case SHORT_TO_DOUBLE:
				javaWriter.i2d();
				break;
			case SHORT_TO_CHAR:
				break;
			case SHORT_TO_STRING:
				if (arguments[0].type.isOptional()) {
					javaWriter.invokeStatic(OBJECTS_TOSTRING);
				} else {
					javaWriter.invokeStatic(SHORT_TO_STRING);
				}
				break;
			case USHORT_TO_BYTE:
				break;
			case USHORT_TO_SBYTE:
				javaWriter.i2b();
				break;
			case USHORT_TO_SHORT:
				javaWriter.i2s();
				break;
			case USHORT_TO_INT:
			case USHORT_TO_UINT:
			case USHORT_TO_USIZE:
				javaWriter.constant(0xFFFF);
				javaWriter.iAnd();
				break;
			case USHORT_TO_LONG:
			case USHORT_TO_ULONG:
				javaWriter.constant(0xFFFF);
				javaWriter.iAnd();
				javaWriter.i2l();
				break;
			case USHORT_TO_FLOAT:
				javaWriter.constant(0xFFFF);
				javaWriter.iAnd();
				javaWriter.i2f();
				break;
			case USHORT_TO_DOUBLE:
				javaWriter.constant(0xFFFF);
				javaWriter.iAnd();
				javaWriter.i2d();
				break;
			case USHORT_TO_CHAR:
				javaWriter.constant(0xFFFF);
				javaWriter.iAnd();
				break;
			case USHORT_TO_STRING:
				if (arguments[0].type.isOptional()) {
					javaWriter.invokeStatic(OBJECTS_TOSTRING);
				} else {
					javaWriter.constant(0xFFFF);
					javaWriter.iAnd();
					javaWriter.invokeStatic(INTEGER_TO_STRING);
				}
				break;
			case ULONG_INVERT:
			case LONG_INVERT:
				javaWriter.invertLong();
				break;
			case BYTE_INVERT:
			case SBYTE_INVERT:
				javaWriter.invertByte();
				break;
			case USIZE_INVERT:
			case UINT_INVERT:
			case INT_INVERT:
				javaWriter.invertInt();
				break;
			case USHORT_INVERT:
			case SHORT_INVERT:
				javaWriter.invertShort();
				break;

			case FLOAT_NEG:
				javaWriter.fNeg();
				break;
			case DOUBLE_NEG:
				javaWriter.dNeg();
				break;
			case INT_NEG:
				javaWriter.iNeg();
				break;
			case LONG_NEG:
				javaWriter.lNeg();
				break;
			case SBYTE_NEG:
				javaWriter.iNeg();
				javaWriter.i2b();
				break;
			case SHORT_NEG:
				javaWriter.iNeg();
				javaWriter.i2s();
				break;
			case INT_TO_BYTE:
			case USIZE_TO_BYTE:
				break;
			case INT_TO_SBYTE:
			case USIZE_TO_SBYTE:
				javaWriter.i2b();
				break;
			case INT_TO_SHORT:
			case USIZE_TO_SHORT:
				javaWriter.i2s();
				break;
			case INT_TO_USHORT:
			case USIZE_TO_USHORT:
				break;
			case INT_TO_UINT:
			case USIZE_TO_INT:
			case USIZE_TO_UINT:
			case INT_TO_USIZE:
				break;
			case INT_TO_LONG:
			case INT_TO_ULONG:
			case USIZE_TO_LONG:
			case USIZE_TO_ULONG:
				javaWriter.i2l();
				break;
			case INT_TO_FLOAT:
			case USIZE_TO_FLOAT:
				javaWriter.i2f();
				break;
			case INT_TO_DOUBLE:
			case USIZE_TO_DOUBLE:
				javaWriter.i2d();
				break;
			case INT_TO_CHAR:
			case USIZE_TO_CHAR:
				javaWriter.i2s();
				break;
			case INT_TO_STRING:
				if (arguments[0].type.isOptional()) {
					javaWriter.invokeStatic(OBJECTS_TOSTRING);
				} else {
					javaWriter.invokeStatic(INTEGER_TO_STRING);
				}
				break;
			case USIZE_TO_STRING:
				if (arguments[0].type.isOptional()) {
					Label ifNull = new Label();
					Label exit = new Label();
					javaWriter.dup();
					javaWriter.constant(-1);
					javaWriter.ifICmpEQ(ifNull);
					javaWriter.invokeStatic(INTEGER_TO_UNSIGNED_STRING);
					javaWriter.goTo(exit);
					javaWriter.label(ifNull);
					javaWriter.pop();
					javaWriter.constant("null");
					javaWriter.label(exit);
				} else {
					javaWriter.invokeStatic(INTEGER_TO_UNSIGNED_STRING);
				}
				break;
			case UINT_TO_BYTE:
				break;
			case UINT_TO_SBYTE:
				javaWriter.i2b();
				break;
			case UINT_TO_SHORT:
				javaWriter.i2s();
				break;
			case UINT_TO_USHORT:
			case UINT_TO_INT:
			case UINT_TO_USIZE:
				break;
			case UINT_TO_LONG:
				javaWriter.i2l();
				break;
			case UINT_TO_ULONG:
				javaWriter.i2l();
				javaWriter.constant(0xFFFFFFFFL);
				javaWriter.lAnd();
				break;
			case UINT_TO_FLOAT:
				javaWriter.i2l();
				javaWriter.constant(0xFFFFFFFFL);
				javaWriter.lAnd();
				javaWriter.l2f();
				break;
			case UINT_TO_DOUBLE:
				javaWriter.i2l();
				javaWriter.constant(0xFFFFFFFFL);
				javaWriter.lAnd();
				javaWriter.l2d();
				break;
			case UINT_TO_CHAR:
				javaWriter.i2s();
				break;
			case UINT_TO_STRING:
				if (arguments[0].type.isOptional()) {
					javaWriter.invokeStatic(OBJECTS_TOSTRING);
				} else {
					javaWriter.invokeStatic(INTEGER_TO_UNSIGNED_STRING);
				}
				break;
			case LONG_TO_BYTE:
				javaWriter.l2i();
				break;
			case LONG_TO_SBYTE:
				javaWriter.l2i();
				javaWriter.i2b();
				break;
			case LONG_TO_SHORT:
				javaWriter.l2i();
				javaWriter.i2s();
				break;
			case LONG_TO_USHORT:
			case LONG_TO_INT:
			case LONG_TO_UINT:
			case LONG_TO_USIZE:
				javaWriter.l2i();
				break;
			case LONG_TO_ULONG:
				break;
			case LONG_TO_FLOAT:
				javaWriter.l2f();
				break;
			case LONG_TO_DOUBLE:
				javaWriter.l2d();
				break;
			case LONG_TO_CHAR:
				javaWriter.l2i();
				javaWriter.i2s();
				break;
			case ULONG_TO_BYTE:
				javaWriter.l2i();
				break;
			case ULONG_TO_SBYTE:
				javaWriter.l2i();
				javaWriter.i2b();
				break;
			case ULONG_TO_SHORT:
				javaWriter.l2i();
				javaWriter.i2s();
				break;
			case ULONG_TO_USHORT:
			case ULONG_TO_INT:
			case ULONG_TO_UINT:
			case ULONG_TO_USIZE:
				javaWriter.l2i();
				break;
			case ULONG_TO_LONG:
				break;
			case ULONG_TO_FLOAT:
				javaWriter.l2f(); // TODO: this is incorrect
				break;
			case ULONG_TO_DOUBLE:
				javaWriter.l2d(); // TODO: this is incorrect
				break;
			case ULONG_TO_CHAR:
				javaWriter.l2i();
				javaWriter.i2s();
				break;
			case FLOAT_TO_BYTE:
				javaWriter.f2i();
				break;
			case FLOAT_TO_SBYTE:
				javaWriter.f2i();
				javaWriter.i2b();
				break;
			case FLOAT_TO_SHORT:
				javaWriter.f2i();
				javaWriter.i2s();
				break;
			case FLOAT_TO_USHORT:
			case FLOAT_TO_UINT:
			case FLOAT_TO_INT:
			case FLOAT_TO_USIZE:
				javaWriter.f2i();
				break;
			case FLOAT_TO_LONG:
			case FLOAT_TO_ULONG:
				javaWriter.f2l();
				break;
			case FLOAT_TO_DOUBLE:
				javaWriter.f2d();
				break;
			case DOUBLE_TO_BYTE:
				javaWriter.d2i();
				break;
			case DOUBLE_TO_SBYTE:
				javaWriter.d2i();
				javaWriter.i2b();
				break;
			case DOUBLE_TO_SHORT:
				javaWriter.d2i();
				javaWriter.i2s();
				break;
			case DOUBLE_TO_USHORT:
			case DOUBLE_TO_INT:
			case DOUBLE_TO_UINT:
			case DOUBLE_TO_USIZE:
				javaWriter.d2i();
				break;
			case DOUBLE_TO_LONG:
			case DOUBLE_TO_ULONG:
				javaWriter.d2l();
				break;
			case DOUBLE_TO_FLOAT:
				javaWriter.d2f();
				break;
			case CHAR_TO_BYTE:
				break;
			case CHAR_TO_SBYTE:
				javaWriter.i2s();
				break;
			case CHAR_TO_SHORT:
			case CHAR_TO_USHORT:
			case CHAR_TO_INT:
			case CHAR_TO_UINT:
			case CHAR_TO_USIZE:
				break;
			case CHAR_TO_LONG:
			case CHAR_TO_ULONG:
				javaWriter.i2l();
				break;
			case CHAR_TO_STRING:
				if (arguments[0].type.isOptional()) {
					javaWriter.invokeStatic(OBJECTS_TOSTRING);
				} else {
					javaWriter.invokeStatic(CHARACTER_TO_STRING);
				}
				break;

			/* Methods and operators */
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
			//case INT_ADD_USIZE:
			//	javaWriter.iAdd();
			//	break;
			case STRING_ADD_STRING:
				javaWriter.invokeVirtual(STRING_CONCAT);
				break;
			case STRING_INDEXGET:
				javaWriter.invokeVirtual(STRING_CHAR_AT);
				break;
			case STRING_TRIM:
				javaWriter.invokeVirtual(STRING_TRIM);
				break;
			case ASSOC_KEYS: {
				Type resultType = context.getType(arguments[0].type);
				javaWriter.invokeVirtual(MAP_KEYS);
				javaWriter.dup();
				javaWriter.invokeVirtual(COLLECTION_SIZE);
				javaWriter.newArray(resultType);
				javaWriter.invokeVirtual(COLLECTION_TOARRAY);
				javaWriter.checkCast(resultType);
				return null;
			}
			case ASSOC_VALUES: {
				Type resultType = context.getType(arguments[0].type);
				javaWriter.invokeVirtual(MAP_VALUES);
				javaWriter.dup();
				javaWriter.invokeVirtual(COLLECTION_SIZE);
				javaWriter.newArray(resultType);
				javaWriter.invokeVirtual(COLLECTION_TOARRAY);
				javaWriter.checkCast(resultType);
				return null;
			}
			case ARRAY_LENGTH1D:
			case ARRAY_DOLLAR1D: {
				javaWriter.arrayLength();
				return null;
			}
			case ARRAY_HASHCODE: {
				ArrayTypeID type = (ArrayTypeID) arguments[0].type;
				if (type.elementType instanceof BasicTypeID) {
					switch ((BasicTypeID) type.elementType) {
						case BOOL:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_BOOLS);
							break;
						case BYTE:
						case SBYTE:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_BYTES);
							break;
						case SHORT:
						case USHORT:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_SHORTS);
							break;
						case INT:
						case UINT:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_INTS);
							break;
						case LONG:
						case ULONG:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_LONGS);
							break;
						case FLOAT:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_FLOATS);
							break;
						case DOUBLE:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_DOUBLES);
							break;
						case CHAR:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_CHARS);
							break;
						default:
							throw new IllegalArgumentException("Unknown basic type: " + type.elementType);
					}
				} else {
					javaWriter.invokeStatic(ARRAYS_DEEPHASHCODE);
				}
				break;
			}
			case ASSOC_CONTAINS:
				javaWriter.invokeVirtual(MAP_CONTAINS_KEY);
				break;
			case ASSOC_INDEXGET: {
				AssocTypeID type = (AssocTypeID) arguments[0].type;
				type.keyType.accept(type.keyType, boxingTypeVisitor);
				javaWriter.invokeInterface(MAP_GET);
				javaWriter.checkCast(context.getType(new OptionalTypeID(type.valueType)));
				break;
			}
			case OPTIONAL_IS_NULL:
				// special case for usize where "null" === -1
				if(arguments[0].type.withoutOptional() == BasicTypeID.USIZE) {
					javaWriter.pop();
					javaWriter.iConstM1();
					Label exit = new Label();
					Label isFalse = new Label();
					javaWriter.ifICmpNE(isFalse);
					javaWriter.iConst1();
					javaWriter.goTo(exit);
					javaWriter.label(isFalse);
					javaWriter.iConst0();
					javaWriter.label(exit);
					break;
				}
				// fallthrough to the other cases which handle all variants except for usize
			case OBJECT_SAME:
			case ASSOC_SAME:
			case ARRAY_SAME: {
				Label exit = new Label();
				Label isFalse = new Label();
				javaWriter.ifACmpNe(isFalse);
				javaWriter.iConst1();
				javaWriter.goTo(exit);
				javaWriter.label(isFalse);
				javaWriter.iConst0();
				javaWriter.label(exit);
				break;
			}
			case OPTIONAL_IS_NOT_NULL:
				// special case for usize where "null" === -1
				if(arguments[0].type.withoutOptional() == BasicTypeID.USIZE) {
					javaWriter.pop();
					javaWriter.iConstM1();
					Label exit = new Label();
					Label isFalse = new Label();
					javaWriter.ifICmpEQ(isFalse);
					javaWriter.iConst1();
					javaWriter.goTo(exit);
					javaWriter.label(isFalse);
					javaWriter.iConst0();
					javaWriter.label(exit);
					break;
				}
				// fallthrough to the other cases which handle all variants except for usize

			case OBJECT_NOTSAME:
			case ASSOC_NOTSAME:
			case ARRAY_NOTSAME: {
				Label exit = new Label();
				Label isFalse = new Label();
				javaWriter.ifACmpEq(isFalse);
				javaWriter.iConst1();
				javaWriter.goTo(exit);
				javaWriter.label(isFalse);
				javaWriter.iConst0();
				javaWriter.label(exit);
				break;
			}
			case ARRAY_ISEMPTY: {
				Label isTrue = new Label();
				Label exit = new Label();

				javaWriter.arrayLength();
				javaWriter.ifEQ(isTrue);
				javaWriter.iConst0();
				javaWriter.goTo(exit);
				javaWriter.label(isTrue);
				javaWriter.iConst1();
				javaWriter.label(exit);
				break;
			}
			case ARRAY_INDEXGET: {
				ArrayTypeID type = arguments[0].type.asArray().orElseThrow(() -> new IllegalStateException("Must be an array"));
				javaWriter.arrayLoad(context.getType(type.elementType));
				break;
			}
			case ARRAY_INDEXSET: {
				ArrayTypeID type = arguments[0].type.asArray().orElseThrow(() -> new IllegalStateException("Must be an array"));
				javaWriter.arrayStore(context.getType(type.elementType));
				break;
			}
			case ENUM_VALUES: {
				DefinitionTypeID type = returnType.asArray().get().elementType.asDefinition().get();
				JavaClass cls = context.getJavaClass(type.definition);
				javaWriter.invokeStatic(JavaNativeMethod.getNativeStatic(cls, "values", "()[L" + cls.internalName + ";"));
				break;
			}
			case RANGE_FROM: {
				RangeTypeID type = (RangeTypeID) arguments[0].type;
				Type jType = context.getType(arguments[0].type);
				javaWriter.getField(jType.getInternalName(), "from", context.getDescriptor(type.baseType));
				break;
			}
			case RANGE_TO:
				RangeTypeID type = (RangeTypeID) arguments[0].type;
				Type jType = context.getType(arguments[0].type);
				javaWriter.getField(jType.getInternalName(), "to", context.getDescriptor(type.baseType));
				break;
			default:
				throw new UnsupportedOperationException("Unknown builtin: " + method);
		}
		return null;
	}

	@Override
	public Void specialConstructor(JavaSpecialMethod method, TypeID type, CallArguments arguments) {
		return null;
	}

	@Override
	public Void specialVirtualMethod(JavaSpecialMethod method, Expression target, CallArguments arguments) {
		return specialStaticMethod(method, arguments.bind(target));
	}

	@Override
	public Void specialStaticMethod(JavaSpecialMethod method, CallArguments args) {
		Expression[] arguments = args.arguments;
		switch (method) {
			case STRINGBUILDER_ISEMPTY:
				arguments[0].accept(expressionVisitor);
				javaWriter.invokeVirtual(STRINGBUILDER_LENGTH);
				break;
			case COLLECTION_TO_ARRAY: {
				Expression value = arguments[0];
				value.accept(expressionVisitor);

				javaWriter.iConst0();
				final Type type = context.getType(((ArrayTypeID) value.type).elementType);
				javaWriter.newArray(type);
				final JavaNativeMethod toArray = new JavaNativeMethod(JavaClass.COLLECTION, JavaNativeMethod.Kind.INSTANCE, "toArray", true, "([Ljava/lang/Object;)[Ljava/lang/Object;", 0, true);
				javaWriter.invokeInterface(toArray);
				javaWriter.checkCast(context.getType(value.type));
				break;
			}
			case CONTAINS_AS_INDEXOF: {
				Expression value = arguments[0];
				value.accept(expressionVisitor);
				arguments[1].accept(expressionVisitor);
				javaWriter.invokeStatic(CHARACTER_TO_STRING);
				javaWriter.invokeInterface(STRING_CONTAINS);
				break;
			}
			case SORTED: {
				// Stack when starting [UnsortedArray, ...]
				// Stack after -> [SortedArray, ...]
				Expression value = arguments[0];
				value.accept(expressionVisitor);
				javaWriter.invokeVirtual(OBJECT_CLONE);
				javaWriter.checkCast(context.getDescriptor(value.type));
				javaWriter.dup();

				// Todo: Primitive method overloads if primitive Array!
				final JavaNativeMethod sort = JavaNativeMethod.getNativeExpansion(JavaClass.ARRAYS, "sort", "([Ljava/lang/Object;)V");
				javaWriter.invokeStatic(sort);
				break;
			}
			case SORTED_WITH_COMPARATOR: {
				Expression value = arguments[0];
				value.accept(expressionVisitor);
				javaWriter.invokeVirtual(OBJECT_CLONE);
				javaWriter.checkCast(context.getDescriptor(value.type));
				javaWriter.dupX1();

				arguments[1].accept(expressionVisitor);
				// ToDo: Primitive Arrays?
				final JavaNativeMethod sortWithComparator = JavaNativeMethod.getNativeExpansion(JavaClass.ARRAYS, "sort", "([Ljava/lang/Object;Ljava/util/Comparator;)V");
				javaWriter.invokeStatic(sortWithComparator);
				break;
			}
			case ARRAY_COPY: {
				Expression value = arguments[0];
				value.accept(expressionVisitor);

				final JavaNativeMethod clone = JavaNativeMethod.getNativeVirtual(JavaClass.OBJECT, "clone", "()Ljava/lang/Object;");
				javaWriter.invokeVirtual(clone);
				javaWriter.checkCast(context.getDescriptor(value.type));
				break;
			}
			case ARRAY_COPY_RESIZE: {
				Expression value = arguments[0];
				value.accept(expressionVisitor);

				final TypeID elementType = ((ArrayTypeID) value.type).elementType;
				final boolean primitive = CompilerUtils.isPrimitive(elementType);
				final String elementDescriptor = primitive
						? context.getDescriptor(elementType)
						: "Ljava/lang/Object;";


				final String methodDescriptor = String.format("([%1$sI)[%1$s", elementDescriptor);

				final JavaNativeMethod copyOf = JavaNativeMethod.getNativeStatic(JavaClass.ARRAYS, "copyOf", methodDescriptor);
				javaWriter.invokeStatic(copyOf);
				if (!primitive) {
					javaWriter.checkCast(context.getDescriptor(value.type));
				}
				break;
			}
			case ARRAY_COPY_TO: {
				Expression value = arguments[0];
				value.accept(expressionVisitor);

				//Copy this (source) to dest
				//              source.copyTo(dest, sourceOffset, destOffset, length)
				//=> System.arraycopy(source, sourceOffset, dest, destOffset, length);
				javaWriter.dup2X2();
				javaWriter.pop2();
				javaWriter.swap();
				javaWriter.dup2X2();
				javaWriter.pop2();
				final JavaClass system = JavaClass.fromInternalName("java/lang/System", JavaClass.Kind.CLASS);
				final JavaNativeMethod javaMethod = JavaNativeMethod.getStatic(system, "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", JavaModifiers.PUBLIC);
				javaWriter.invokeStatic(javaMethod);
				break;
			}
			case STRING_TO_ASCII: {
				Expression value = arguments[0];
				value.accept(expressionVisitor);

				final JavaClass standardCharsets = JavaClass.fromInternalName("java/nio/charset/StandardCharsets", JavaClass.Kind.CLASS);
				final JavaNativeField charset = new JavaNativeField(standardCharsets, "US_ASCII", "Ljava/nio/charset/Charset;");
				javaWriter.getStaticField(charset);
				javaWriter.invokeVirtual(STRING_GET_BYTES);
				break;
			}
			case STRING_TO_UTF8: {
				Expression value = arguments[0];
				value.accept(expressionVisitor);

				final JavaClass standardCharsets = JavaClass.fromInternalName("java/nio/charset/StandardCharsets", JavaClass.Kind.CLASS);
				final JavaNativeField charset = new JavaNativeField(standardCharsets, "UTF_8", "Ljava/nio/charset/Charset;");
				javaWriter.getStaticField(charset);
				javaWriter.invokeVirtual(STRING_GET_BYTES);
				break;
			}
			case BYTES_ASCII_TO_STRING: {
				Expression value = arguments[0];
				value.accept(expressionVisitor);

				final JavaClass standardCharsets = JavaClass.fromInternalName("java/nio/charset/StandardCharsets", JavaClass.Kind.CLASS);
				final JavaNativeField charset = new JavaNativeField(standardCharsets, "US_ASCII", "Ljava/nio/charset/Charset;");

				javaWriter.newObject(JavaClass.STRING);
				javaWriter.dupX1();
				javaWriter.swap();
				javaWriter.getStaticField(charset);
				javaWriter.invokeSpecial(STRING_INIT_BYTES_CHARSET);
				break;
			}
			case BYTES_UTF8_TO_STRING: {
				Expression value = arguments[0];
				value.accept(expressionVisitor);

				final JavaClass standardCharsets = JavaClass.fromInternalName("java/nio/charset/StandardCharsets", JavaClass.Kind.CLASS);
				final JavaNativeField charset = new JavaNativeField(standardCharsets, "UTF_8", "Ljava/nio/charset/Charset;");

				javaWriter.newObject(JavaClass.STRING);
				javaWriter.dupX1();
				javaWriter.swap();
				javaWriter.getStaticField(charset);
				javaWriter.invokeSpecial(STRING_INIT_BYTES_CHARSET);
				break;
			}
		}

		return null;
	}
}
