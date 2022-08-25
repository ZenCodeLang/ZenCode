package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javabytecode.compiler.JavaModificationExpressionVisitor.PushOption;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.javashared.expressions.JavaFunctionInterfaceCastExpression;
import org.openzen.zenscript.javashared.types.JavaFunctionalInterfaceTypeID;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class JavaExpressionVisitor implements ExpressionVisitor<Void> {
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

	final JavaWriter javaWriter;
	final JavaBytecodeContext context;
	final JavaCompiledModule module;
	private final JavaBoxingTypeVisitor boxingTypeVisitor;
	private final JavaUnboxingTypeVisitor unboxingTypeVisitor;
	private final JavaCapturedExpressionVisitor capturedExpressionVisitor = new JavaCapturedExpressionVisitor(this);
	private final JavaMethodBytecodeCompiler methodCompiler;

	public JavaExpressionVisitor(JavaBytecodeContext context, JavaCompiledModule module, JavaWriter javaWriter) {
		this.javaWriter = javaWriter;
		this.context = context;
		this.module = module;
		boxingTypeVisitor = new JavaBoxingTypeVisitor(javaWriter);
		unboxingTypeVisitor = new JavaUnboxingTypeVisitor(javaWriter);
		methodCompiler = new JavaMethodBytecodeCompiler(javaWriter, this, context, module);
	}

	//TODO replace with visitor?
	private static int calculateMemberPosition(GetLocalVariableExpression localVariableExpression, FunctionExpression expression) {
		int h = 1;//expression.header.parameters.length;
		for (CapturedExpression capture : expression.closure.captures) {
			if (capture instanceof CapturedLocalVariableExpression && ((CapturedLocalVariableExpression) capture).variable == localVariableExpression.variable)
				return h;
			if (capture instanceof CapturedClosureExpression && ((CapturedClosureExpression) capture).value instanceof CapturedLocalVariableExpression && ((CapturedLocalVariableExpression) ((CapturedClosureExpression) capture).value).variable == localVariableExpression.variable)
				return h;
			h++;
		}
		throw new RuntimeException(localVariableExpression.position.toString() + ": Captured Statement error");
	}

	private static int calculateMemberPosition(CapturedParameterExpression functionParameterExpression, FunctionExpression expression) {
		int h = 1;//expression.header.parameters.length;
		for (CapturedExpression capture : expression.closure.captures) {
			if (capture instanceof CapturedParameterExpression && ((CapturedParameterExpression) capture).parameter == functionParameterExpression.parameter)
				return h;
			h++;
		}
		throw new RuntimeException(functionParameterExpression.position.toString() + ": Captured Statement error");
	}

	private static boolean hasNoDefault(MatchExpression switchStatement) {
		for (MatchExpression.Case switchCase : switchStatement.cases)
			if (switchCase.key == null) return false;
		return true;
	}

	@Override
	public Void visitAndAnd(AndAndExpression expression) {
		Label end = new Label();
		Label onFalse = new Label();

		expression.left.accept(this);

		javaWriter.ifEQ(onFalse);
		expression.right.accept(this);

		// //these two calls are redundant but make decompiled code look better. Keep?
		// javaWriter.ifEQ(onFalse);
		// javaWriter.iConst1();

		javaWriter.goTo(end);

		javaWriter.label(onFalse);
		javaWriter.iConst0();


		javaWriter.label(end);

		return null;
	}

	@Override
	public Void visitArray(ArrayExpression expression) {
		javaWriter.constant(expression.expressions.length);
		Type type = context.getType(((ArrayTypeID) expression.type).elementType);
		javaWriter.newArray(type);
		for (int i = 0; i < expression.expressions.length; i++) {
			javaWriter.dup();
			javaWriter.constant(i);
			expression.expressions[i].accept(this);
			javaWriter.arrayStore(type);
		}
		return null;
	}

	@Override
	public Void visitCompare(CompareExpression expression) {
		if (expression.operator.method instanceof BuiltinMethodSymbol) {
			BuiltinMethodSymbol method = (BuiltinMethodSymbol) expression.operator.method;
			switch (method) {
				case BYTE_COMPARE:
					expression.left.accept(this);
					javaWriter.constant(0xFF);
					javaWriter.iAnd();
					expression.right.accept(this);
					javaWriter.constant(0xFF);
					javaWriter.iAnd();
					compareInt(expression.comparison);
					break;
				case USHORT_COMPARE:
					expression.left.accept(this);
					javaWriter.constant(0xFFFF);
					javaWriter.iAnd();
					expression.right.accept(this);
					javaWriter.constant(0xFFFF);
					javaWriter.iAnd();
					compareInt(expression.comparison);
					break;
				case SBYTE_COMPARE:
				case SHORT_COMPARE:
				case INT_COMPARE:
				case CHAR_COMPARE:
				case USIZE_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					compareInt(expression.comparison);
					break;
				case UINT_COMPARE:
				case USIZE_COMPARE_UINT:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.invokeStatic(INTEGER_COMPARE_UNSIGNED);
					compareGeneric(expression.comparison);
					break;
				case LONG_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.invokeStatic(LONG_COMPARE);
					compareGeneric(expression.comparison);
					break;
				case ULONG_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.invokeStatic(LONG_COMPARE_UNSIGNED);
					compareGeneric(expression.comparison);
					break;
				case ULONG_COMPARE_UINT:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.i2l();
					javaWriter.constant(0xFFFF_FFFFL);
					javaWriter.lAnd();
					javaWriter.invokeStatic(LONG_COMPARE_UNSIGNED);
					compareGeneric(expression.comparison);
					break;
				case ULONG_COMPARE_USIZE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.i2l();
					javaWriter.invokeStatic(LONG_COMPARE_UNSIGNED);
					compareGeneric(expression.comparison);
					break;
				case FLOAT_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.invokeStatic(FLOAT_COMPARE);
					compareGeneric(expression.comparison);
					break;
				case DOUBLE_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.invokeStatic(DOUBLE_COMPARE);
					compareGeneric(expression.comparison);
					break;
				case STRING_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.invokeVirtual(STRING_COMPARETO);
					compareGeneric(expression.comparison);
					break;
				case ENUM_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.invokeVirtual(ENUM_COMPARETO);
					compareGeneric(expression.comparison);
					break;
				default:
					throw new UnsupportedOperationException("Unknown builtin comparator: " + method);
			}
		} else {
			JavaMethod method = context.getJavaMethod(expression.operator.method);
			method.compileStatic(methodCompiler, BasicTypeID.INT, new CallArguments(expression.left, expression.right));
			compareGeneric(expression.comparison);
		}

		return null;
	}

	private void compareInt(CompareType comparator) {
		Label exit = new Label();
		Label isTrue = new Label();
		switch (comparator) {
			case EQ:
				javaWriter.ifICmpEQ(isTrue);
				break;
			case NE:
				javaWriter.ifICmpNE(isTrue);
				break;
			case GT:
				javaWriter.ifICmpGT(isTrue);
				break;
			case GE:
				javaWriter.ifICmpGE(isTrue);
				break;
			case LT:
				javaWriter.ifICmpLT(isTrue);
				break;
			case LE:
				javaWriter.ifICmpLE(isTrue);
				break;
			default:
				throw new IllegalStateException("Invalid comparator: " + comparator);
		}
		javaWriter.iConst0();
		javaWriter.goTo(exit);
		javaWriter.label(isTrue);
		javaWriter.iConst1();
		javaWriter.label(exit);
	}

	private void compareGeneric(CompareType comparator) {
		Label exit = new Label();
		Label isTrue = new Label();
		switch (comparator) {
			case EQ:
				javaWriter.ifEQ(isTrue);
				break;
			case NE:
				javaWriter.ifNE(isTrue);
				break;
			case GT:
				javaWriter.ifGT(isTrue);
				break;
			case GE:
				javaWriter.ifGE(isTrue);
				break;
			case LT:
				javaWriter.ifLT(isTrue);
				break;
			case LE:
				javaWriter.ifLE(isTrue);
				break;
			default:
				throw new IllegalStateException("Invalid comparator: " + comparator);
		}
		javaWriter.iConst0();
		javaWriter.goTo(exit);
		javaWriter.label(isTrue);
		javaWriter.iConst1();
		javaWriter.label(exit);
	}

	@Override
	public Void visitCall(CallExpression expression) {
		Module module = expression.method.method.getDefiningType().getModule();
		JavaCompiledModule javaCompiledModule = context.getJavaModule(module);
		JavaMethod method = javaCompiledModule.getMethodInfo(expression.method.method);
		method.compileVirtual(methodCompiler, expression.type, expression.target, expression.arguments);
		return null;
	}

	@Override
	public Void visitCallStatic(CallStaticExpression expression) {
		JavaMethod method = context.getJavaMethod(expression.member.method);
		method.compileStatic(methodCompiler, expression.type, expression.arguments);
		return null;
	}

	private void handleCallArguments(Expression[] arguments, FunctionParameter[] parameters, boolean variadic, FunctionParameter[] originalParameters) {
		if (variadic) {
			for (int i = 0; i < parameters.length - 1; i++) {
				arguments[i].accept(this);
				if (originalParameters[i].type.isGeneric()) {
					if (CompilerUtils.isPrimitive(parameters[i].type)) {
						parameters[i].type.accept(parameters[i].type, boxingTypeVisitor);
					}
				}
			}

			final int arrayCount = (arguments.length - parameters.length) + 1;
			javaWriter.constant(arrayCount);
			javaWriter.newArray(context.getType(parameters[parameters.length - 1].type).getElementType());
			for (int i = 0; i < arrayCount; i++) {
				javaWriter.dup();
				javaWriter.constant(i);
				final Expression argument = arguments[i + parameters.length - 1];
				argument.accept(this);
				javaWriter.arrayStore(context.getType(argument.type));
			}


		} else {
			for (int i = 0; i < arguments.length; i++) {
				arguments[i].accept(this);
				if (originalParameters[i].type.isGeneric()) {
					if (CompilerUtils.isPrimitive(parameters[i].type)) {
						parameters[i].type.accept(parameters[i].type, boxingTypeVisitor);
					}
				}
			}
		}
	}

	private void handleReturnValue(TypeID original, TypeID actual) {
		if (original.isGeneric()) {
			handleGenericReturnValue(actual);
		}
	}

	private void handleGenericReturnValue(TypeID actual) {
		if (CompilerUtils.isPrimitive(actual)) {
			getJavaWriter().checkCast(context.getInternalName(new OptionalTypeID(null, actual)));
			actual.accept(actual, unboxingTypeVisitor);
		} else {
			getJavaWriter().checkCast(context.getInternalName(actual));
		}
	}

	@Override
	public Void visitCapturedClosure(CapturedClosureExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	@Override
	public Void visitCapturedLocalVariable(CapturedLocalVariableExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	@Override
	public Void visitCapturedParameter(CapturedParameterExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	@Override
	public Void visitCapturedThis(CapturedThisExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	@Override
	public Void visitCheckNull(CheckNullExpression expression) {
		final Label end = new Label();
		expression.value.accept(this);
		javaWriter.dup();
		javaWriter.ifNonNull(end);
		javaWriter.pop();
		javaWriter.newObject("java/lang/NullPointerException");
		javaWriter.dup();
		javaWriter.constant("Tried to convert a null value to nonnull type " + context.getType(expression.type).getClassName());
		javaWriter.invokeSpecial(NullPointerException.class, "<init>", "(Ljava/lang/String;)V");
		javaWriter.aThrow();
		javaWriter.label(end);

		return null;
	}

	@Override
	public Void visitCoalesce(CoalesceExpression expression) {
		final Label end = new Label();
		expression.left.accept(this);
		javaWriter.dup();
		javaWriter.ifNonNull(end);
		javaWriter.pop();
		expression.right.accept(this);
		expression.right.type.accept(expression.right.type, boxingTypeVisitor);
		javaWriter.label(end);
		expression.type.accept(expression.type, unboxingTypeVisitor);
		return null;
	}

	@Override
	public Void visitConditional(ConditionalExpression expression) {
		final Label end = new Label();
		final Label onElse = new Label();
		expression.condition.accept(this);
		javaWriter.ifEQ(onElse);
		expression.ifThen.accept(this);
		javaWriter.goTo(end);
		javaWriter.label(onElse);
		expression.ifElse.accept(this);
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitConst(ConstExpression expression) {
		BuiltinID builtin = expression.constant.member.builtin;
		if (builtin == null) {
			javaWriter.getStaticField(context.getJavaField(expression.constant));
			return null;
		}

		switch (builtin) {
			case BYTE_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case BYTE_GET_MAX_VALUE:
				javaWriter.constant(0xFF);
				break;
			case SBYTE_GET_MIN_VALUE:
				javaWriter.getStaticField(BYTE_MIN_VALUE);
				break;
			case SBYTE_GET_MAX_VALUE:
				javaWriter.getStaticField(BYTE_MAX_VALUE);
				break;
			case SHORT_GET_MIN_VALUE:
				javaWriter.getStaticField(SHORT_MIN_VALUE);
				break;
			case SHORT_GET_MAX_VALUE:
				javaWriter.getStaticField(SHORT_MAX_VALUE);
				break;
			case USHORT_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case USHORT_GET_MAX_VALUE:
				javaWriter.constant(0xFFFF);
				break;
			case INT_GET_MIN_VALUE:
				javaWriter.getStaticField(INTEGER_MIN_VALUE);
				break;
			case INT_GET_MAX_VALUE:
				javaWriter.getStaticField(INTEGER_MAX_VALUE);
				break;
			case UINT_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case UINT_GET_MAX_VALUE:
				javaWriter.constant(-1);
				break;
			case LONG_GET_MIN_VALUE:
				javaWriter.getStaticField(LONG_MIN_VALUE);
				break;
			case LONG_GET_MAX_VALUE:
				javaWriter.getStaticField(LONG_MAX_VALUE);
				break;
			case ULONG_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case ULONG_GET_MAX_VALUE:
				javaWriter.constant(-1L);
				break;
			case USIZE_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case USIZE_GET_MAX_VALUE:
				javaWriter.getStaticField(INTEGER_MAX_VALUE);
				break;
			case USIZE_BITS:
				javaWriter.constant(32);
				break;
			case FLOAT_GET_MIN_VALUE:
				javaWriter.getStaticField(FLOAT_MIN_VALUE);
				break;
			case FLOAT_GET_MAX_VALUE:
				javaWriter.getStaticField(FLOAT_MAX_VALUE);
				break;
			case DOUBLE_GET_MIN_VALUE:
				javaWriter.getStaticField(DOUBLE_MIN_VALUE);
				break;
			case DOUBLE_GET_MAX_VALUE:
				javaWriter.getStaticField(DOUBLE_MAX_VALUE);
				break;
			case CHAR_GET_MIN_VALUE:
				javaWriter.getStaticField(CHARACTER_MIN_VALUE);
				break;
			case CHAR_GET_MAX_VALUE:
				javaWriter.getStaticField(CHARACTER_MAX_VALUE);
				break;
			case ENUM_VALUES: {
				DefinitionTypeID type = (DefinitionTypeID) ((ArrayTypeID) expression.type).elementType;
				JavaClass cls = context.getJavaClass(type.definition);
				javaWriter.invokeStatic(JavaNativeMethod.getNativeStatic(cls, "values", "()[L" + cls.internalName + ";"));
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown builtin: " + builtin);
		}

		return null;
	}

	@Override
	public Void visitConstantBool(ConstantBoolExpression expression) {
		if (expression.value)
			javaWriter.iConst1();
		else
			javaWriter.iConst0();
		return null;
	}

	@Override
	public Void visitConstantByte(ConstantByteExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantChar(ConstantCharExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantDouble(ConstantDoubleExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantFloat(ConstantFloatExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantInt(ConstantIntExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantLong(ConstantLongExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantSByte(ConstantSByteExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantShort(ConstantShortExpression expression) {
		getJavaWriter().siPush(expression.value);
		return null;
	}

	@Override
	public Void visitConstantString(ConstantStringExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUInt(ConstantUIntExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantULong(ConstantULongExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUShort(ConstantUShortExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUSize(ConstantUSizeExpression expression) {
		getJavaWriter().constant((int) expression.value);
		return null;
	}

	@Override
	public Void visitConstructorThisCall(ConstructorThisCallExpression expression) {
		throw new UnsupportedOperationException("Invalid usage");
	}

	@Override
	public Void visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
		throw new UnsupportedOperationException("Invalid usage");
	}

	@Override
	public Void visitEnumConstant(EnumConstantExpression expression) {
		javaWriter.getStaticField(context.getInternalName(expression.type), module.getEnumMapper().getMapping(expression.value).orElseGet(() -> expression.value.name), context.getDescriptor(expression.type));
		return null;
	}

	@Override
	public Void visitFunction(FunctionExpression expression) {
		CompilerUtils.tagMethodParameters(context, module, expression.header, false, Collections.emptyList());

        /*if (expression.header.parameters.length == 0 && expression.body instanceof ReturnStatement && expression.body.hasTag(MatchExpression.class) && expression.closure.captures.isEmpty()) {
            ((ReturnStatement) expression.body).value.accept(this);
            return null;
        }*/

		final String signature;
		final String[] interfaces;
		final String descriptor;

		{//Fill the info above
			if (expression.type instanceof JavaFunctionalInterfaceTypeID) {
				//Let's implement the functional Interface instead
				JavaFunctionalInterfaceTypeID type = (JavaFunctionalInterfaceTypeID) expression.type;
				final Method functionalInterfaceMethod = type.functionalInterfaceMethod;

				//Should be the same, should it not?
				signature = context.getMethodSignature(expression.header, true);
				descriptor = context.getMethodDescriptor(expression.header);
				interfaces = new String[]{Type.getInternalName(functionalInterfaceMethod.getDeclaringClass())};
			} else {
				//Normal way, no casting to functional interface
				signature = context.getMethodSignature(expression.header, true);
				descriptor = context.getMethodDescriptor(expression.header);
				interfaces = new String[]{context.getInternalName(new FunctionTypeID(null, expression.header))};
			}
		}

		final JavaNativeMethod methodInfo;
		// We don't allow registering classes starting with "java"
		final String className = interfaces[0].replace("java", "j").replace("/", "_") + "_" + context.getLambdaCounter();
		{
			final JavaNativeMethod m = context.getFunctionalInterface(expression.type);
			methodInfo = new JavaNativeMethod(m.cls, m.kind, m.name, m.compile, m.descriptor, m.modifiers & ~JavaModifiers.ABSTRACT, m.genericResult, m.typeParameterArguments);
		}
		final ClassWriter lambdaCW = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
		lambdaCW.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", interfaces);
		final JavaWriter functionWriter;

		//Bridge method!!!
		if (!Objects.equals(methodInfo.descriptor, descriptor)) {
			final JavaNativeMethod bridgeMethodInfo = new JavaNativeMethod(methodInfo.cls, methodInfo.kind, methodInfo.name, methodInfo.compile, methodInfo.descriptor, methodInfo.modifiers | JavaModifiers.BRIDGE | JavaModifiers.SYNTHETIC, methodInfo.genericResult, methodInfo.typeParameterArguments);
			final JavaWriter bridgeWriter = new JavaWriter(context.logger, expression.position, lambdaCW, bridgeMethodInfo, null, methodInfo.descriptor, null, "java/lang/Override");
			bridgeWriter.start();

			//This.name(parameters, casted)
			bridgeWriter.loadObject(0);

			for (int i = 0; i < expression.header.parameters.length; i++) {
				final FunctionParameter functionParameter = expression.header.parameters[i];
				final Type type = context.getType(functionParameter.type);
				bridgeWriter.load(type, i + 1);
				if (!CompilerUtils.isPrimitive(functionParameter.type)) {
					bridgeWriter.checkCast(type);
				}
			}

			bridgeWriter.invokeVirtual(new JavaNativeMethod(JavaClass.fromInternalName(className, JavaClass.Kind.CLASS), JavaNativeMethod.Kind.INSTANCE, methodInfo.name, methodInfo.compile, descriptor, methodInfo.modifiers, methodInfo.genericResult));
			final TypeID returnType = expression.header.getReturnType();
			if (returnType != BasicTypeID.VOID) {
				final Type returnTypeASM = context.getType(returnType);
				if (!CompilerUtils.isPrimitive(returnType)) {
					bridgeWriter.checkCast(returnTypeASM);
				}
				bridgeWriter.returnType(returnTypeASM);
			}

			bridgeWriter.ret();
			bridgeWriter.end();


			final JavaNativeMethod actualMethod = new JavaNativeMethod(methodInfo.cls, methodInfo.kind, methodInfo.name, methodInfo.compile, context.getMethodDescriptor(expression.header), methodInfo.modifiers, methodInfo.genericResult, methodInfo.typeParameterArguments);
			//No @Override
			functionWriter = new JavaWriter(context.logger, expression.position, lambdaCW, actualMethod, null, signature, null);
		} else {
			functionWriter = new JavaWriter(context.logger, expression.position, lambdaCW, methodInfo, null, signature, null, "java/lang/Override");
		}

		javaWriter.newObject(className);
		javaWriter.dup();

		final String constructorDesc = calcFunctionSignature(expression.closure);


		final JavaWriter constructorWriter = new JavaWriter(context.logger, expression.position, lambdaCW, JavaNativeMethod.getConstructor(javaWriter.method.cls, constructorDesc, Opcodes.ACC_PUBLIC), null, null, null);
		constructorWriter.start();
		constructorWriter.loadObject(0);
		constructorWriter.dup();
		constructorWriter.invokeSpecial(Object.class, "<init>", "()V");

		int i = 0;
		for (CapturedExpression capture : expression.closure.captures) {
			constructorWriter.dup();
			Type type = context.getType(capture.type);
			lambdaCW.visitField(Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE, "captured" + ++i, type.getDescriptor(), null, null).visitEnd();

			capture.accept(this);

			constructorWriter.load(type, i);
			constructorWriter.putField(className, "captured" + i, type.getDescriptor());
		}

		constructorWriter.pop();

		javaWriter.invokeSpecial(className, "<init>", constructorDesc);

		constructorWriter.ret();
		constructorWriter.end();


		functionWriter.start();


		final JavaStatementVisitor CSV = new JavaStatementVisitor(context, new JavaExpressionVisitor(context, module, functionWriter) {
			@Override
			public Void visitGetLocalVariable(GetLocalVariableExpression varExpression) {
				final JavaLocalVariableInfo localVariable = functionWriter.tryGetLocalVariable(varExpression.variable.variable);
				if (localVariable != null) {
					final Label label = new Label();
					localVariable.end = label;
					functionWriter.label(label);
					functionWriter.load(localVariable);
					return null;
				}


				final int position = calculateMemberPosition(varExpression, expression);
				functionWriter.loadObject(0);
				functionWriter.getField(className, "captured" + position, context.getDescriptor(varExpression.variable.type));
				return null;
			}

			@Override
			public Void visitCapturedParameter(CapturedParameterExpression varExpression) {
				final int position = calculateMemberPosition(varExpression, expression);
				functionWriter.loadObject(0);
				functionWriter.getField(className, "captured" + position, context.getDescriptor(varExpression.parameter.type));
				return null;
			}
		});

		expression.body.accept(CSV);

		functionWriter.ret();
		functionWriter.end();
		lambdaCW.visitEnd();

		context.register(className, lambdaCW.toByteArray());

		return null;
	}

	private String calcFunctionSignature(LambdaClosure closure) {
		StringJoiner joiner = new StringJoiner("", "(", ")V");
		for (CapturedExpression capture : closure.captures) {
			String descriptor = context.getDescriptor(capture.type);
			joiner.add(descriptor);
		}
		return joiner.toString();
	}

	@Override
	public Void visitGetField(GetFieldExpression expression) {
		expression.target.accept(this);
		getField(expression.field);
		handleReturnValue(expression.field.member.getType(), expression.field.getType());
		return null;
	}

	@Override
	public Void visitGetFunctionParameter(GetFunctionParameterExpression expression) {
		JavaParameterInfo parameter = module.getParameterInfo(expression.parameter);

		if (parameter == null)
			throw new RuntimeException(expression.position.toString() + ": Could not resolve lambda parameter" + expression.parameter);

		javaWriter.load(context.getType(expression.parameter.type), parameter.index);
		return null;
	}

	@Override
	public Void visitGetLocalVariable(GetLocalVariableExpression expression) {
		final Label label = new Label();
		final JavaLocalVariableInfo tag = javaWriter.getLocalVariable(expression.variable.variable);

		tag.end = label;
		javaWriter.load(tag.type, tag.local);
		javaWriter.label(label);
		return null;
	}

	@Override
	public Void visitGetMatchingVariantField(GetMatchingVariantField expression) {
		javaWriter.loadObject(0);
		final TypeID type = expression.value.option.getParameterType(expression.index);
		final JavaVariantOption tag = context.getJavaVariantOption(expression.value.option);
		javaWriter.checkCast(tag.variantOptionClass.internalName);
		javaWriter.getField(new JavaField(tag.variantOptionClass, "field" + expression.index, context.getDescriptor(type)));
		return null;
	}

	@Override
	public Void visitGetStaticField(GetStaticFieldExpression expression) {
		javaWriter.getStaticField(context.getJavaField(expression.field));
		return null;
	}

	@Override
	public Void visitGetter(GetterExpression expression) {

		expression.target.accept(this);

		Module module = expression.getter.method.getDefiningType().getModule();
		JavaCompiledModule javaCompiledModule = context.getJavaModule(module);
		JavaMethod method = javaCompiledModule.getMethodInfo(expression.getter.method);
		method.compileVirtual(methodCompiler, expression.type, expression.target, CallArguments.EMPTY);

		switch (builtin) {
			case FLOAT_BITS:
				javaWriter.invokeStatic(FLOAT_BITS);
				break;
			case DOUBLE_BITS:
				javaWriter.invokeStatic(DOUBLE_BITS);
				break;
			case STRING_LENGTH:
				javaWriter.invokeVirtual(STRING_LENGTH);
				break;
			case STRING_CHARACTERS:
				javaWriter.invokeVirtual(STRING_CHARACTERS);
				break;
			case STRING_ISEMPTY:
				javaWriter.invokeVirtual(STRING_ISEMPTY);
				break;
			case ASSOC_SIZE:
				javaWriter.invokeVirtual(MAP_SIZE);
				break;
			case ASSOC_ISEMPTY:
				javaWriter.invokeVirtual(MAP_ISEMPTY);
				break;
			case ASSOC_KEYS: {
				Type resultType = context.getType(expression.type);

				javaWriter.invokeVirtual(MAP_KEYS);
				javaWriter.dup();
				javaWriter.invokeVirtual(COLLECTION_SIZE);
				javaWriter.newArray(resultType);
				javaWriter.invokeVirtual(COLLECTION_TOARRAY);
				javaWriter.checkCast(resultType);
				break;
			}
			case ASSOC_VALUES: {
				Type resultType = context.getType(expression.type);

				javaWriter.invokeVirtual(MAP_VALUES);
				javaWriter.dup();
				javaWriter.invokeVirtual(COLLECTION_SIZE);
				javaWriter.newArray(resultType);
				javaWriter.invokeVirtual(COLLECTION_TOARRAY);
				javaWriter.checkCast(resultType);
				break;
			}
			case ASSOC_HASHCODE:
				// TODO: we need a content-based hashcode
				javaWriter.invokeVirtual(OBJECT_HASHCODE);
				break;
			case GENERICMAP_HASHCODE:
				// TODO: we need a content-based hashcode
				javaWriter.invokeVirtual(OBJECT_HASHCODE);
				break;
			case ARRAY_LENGTH:
				javaWriter.arrayLength();
				break;
			case ARRAY_HASHCODE: {
				ArrayTypeID type = (ArrayTypeID) expression.target.type;
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
			case ARRAY_ISEMPTY:
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
			case ENUM_NAME:
				javaWriter.invokeVirtual(ENUM_NAME);
				break;
			case ENUM_ORDINAL:
				javaWriter.invokeVirtual(ENUM_ORDINAL);
				break;
			case OBJECT_HASHCODE:
				javaWriter.invokeVirtual(OBJECT_HASHCODE);
				break;
			case RANGE_FROM: {
				RangeTypeID type = (RangeTypeID) expression.target.type;
				Type jType = context.getType(expression.target.type);
				javaWriter.getField(jType.getInternalName(), "from", context.getDescriptor(type.baseType));
				break;
			}
			case RANGE_TO:
				RangeTypeID type = (RangeTypeID) expression.target.type;
				Type jType = context.getType(expression.target.type);
				javaWriter.getField(jType.getInternalName(), "to", context.getDescriptor(type.baseType));
				break;
		}

		return null;
	}

	@Override
	public Void visitGlobal(GlobalExpression expression) {
		return expression.resolution.accept(this);
	}

	@Override
	public Void visitGlobalCall(GlobalCallExpression expression) {
		return expression.resolution.accept(this);
	}

	@Override
	public Void visitInterfaceCast(InterfaceCastExpression expression) {
		expression.value.accept(this);
		javaWriter.checkCast(context.getInternalName(expression.type));
		return null;
	}

	@Override
	public Void visitIs(IsExpression expression) {
		expression.value.accept(this);
		javaWriter.instanceOf(context.getInternalName(expression.isType));
		return null;
	}

	@Override
	public Void visitMakeConst(MakeConstExpression expression) {
		return null;
	}

	@Override
	public Void visitMap(MapExpression expression) {
		javaWriter.newObject("java/util/HashMap");
		javaWriter.dup();
		javaWriter.invokeSpecial("java/util/HashMap", "<init>", "()V");
		final AssocTypeID type = (AssocTypeID) expression.type;
		for (int i = 0; i < expression.keys.length; i++) {
			javaWriter.dup();
			expression.keys[i].accept(this);
			type.keyType.accept(type.keyType, boxingTypeVisitor);
			expression.values[i].accept(this);
			type.valueType.accept(type.valueType, boxingTypeVisitor);
			javaWriter.invokeInterface(MAP_PUT);
			javaWriter.pop();
		}
		return null;
	}

	@Override
	public Void visitMatch(MatchExpression expression) {
		final Label start = new Label();
		final Label end = new Label();

		javaWriter.label(start);
		expression.value.accept(this);

		//TODO replace beforeSwitch visitor or similar
		if (expression.value.type == BasicTypeID.STRING)
			javaWriter.invokeVirtual(OBJECT_HASHCODE);

		//TODO replace with beforeSwitch visitor or similar
		for (MatchExpression.Case aCase : expression.cases) {
			if (aCase.key instanceof VariantOptionSwitchValue) {
				VariantOptionSwitchValue variantOptionSwitchValue = (VariantOptionSwitchValue) aCase.key;
				JavaVariantOption option = context.getJavaVariantOption(variantOptionSwitchValue.option);
				javaWriter.invokeVirtual(JavaNativeMethod.getNativeVirtual(option.variantClass, "getDenominator", "()I"));
				break;
			}
		}

		final boolean hasNoDefault = hasNoDefault(expression);

		final MatchExpression.Case[] cases = expression.cases;
		final JavaSwitchLabel[] switchLabels = new JavaSwitchLabel[hasNoDefault ? cases.length : cases.length - 1];
		final Label defaultLabel = new Label();

		int i = 0;
		for (final MatchExpression.Case matchCase : cases) {
			if (matchCase.key != null) {
				switchLabels[i++] = new JavaSwitchLabel(CompilerUtils.getKeyForSwitch(matchCase.key), new Label());
			}
		}

		JavaSwitchLabel[] sortedSwitchLabels = Arrays.copyOf(switchLabels, switchLabels.length);
		Arrays.sort(sortedSwitchLabels, Comparator.comparingInt(a -> a.key));

		javaWriter.lookupSwitch(defaultLabel, sortedSwitchLabels);

		i = 0;
		for (final MatchExpression.Case switchCase : cases) {
			if (hasNoDefault || switchCase.key != null) {
				javaWriter.label(switchLabels[i++].label);
			} else {
				javaWriter.label(defaultLabel);
			}
			//switchCase.value.body.setTag(MatchExpression.class, expression);
			switchCase.value.accept(this);
			javaWriter.goTo(end);
		}

		if (hasNoDefault) {
			javaWriter.label(defaultLabel);
			if (context.getType(expression.type).getOpcode(Opcodes.ISTORE) == Opcodes.ASTORE)
				javaWriter.aConstNull();
			else
				javaWriter.iConst0();
		}

		javaWriter.label(end);

		// TODO: what's this one exactly for?
		if (!CompilerUtils.isPrimitive(expression.type)) {
			javaWriter.checkCast(context.getType(expression.type));
		}
		return null;
	}

	@Override
	public Void visitNew(NewExpression expression) {
		JavaMethod method = context.getJavaMethod(expression.constructor.method);
		method.compileConstructor(methodCompiler, expression.type, expression.arguments);
		return null;
	}

	@Override
	public Void visitNull(NullExpression expression) {
		if (expression.type != BasicTypeID.NULL && expression.type.withoutOptional() == BasicTypeID.USIZE) {
			javaWriter.constant(-1); // special case: usize? null = -1
		} else {
			javaWriter.aConstNull();
		}
		return null;
	}

	@Override
	public Void visitOrOr(OrOrExpression expression) {
		Label end = new Label();
		Label onTrue = new Label();

		expression.left.accept(this);

		javaWriter.ifNE(onTrue);
		expression.right.accept(this);

		// //these two calls are redundant but make decompiled code look better. Keep?
		// javaWriter.ifNE(onTrue);
		// javaWriter.iConst0();

		javaWriter.goTo(end);

		javaWriter.label(onTrue);
		javaWriter.iConst1();


		javaWriter.label(end);

		return null;
	}

	@Override
	public Void visitPanic(PanicExpression expression) {
		javaWriter.newObject("java/lang/AssertionError");
		javaWriter.dup();
		expression.value.accept(this);
		javaWriter.invokeSpecial(AssertionError.class, "<init>", "(Ljava/lang/Object;)V");
		javaWriter.aThrow();
		return null;
	}

	private void modify(Expression source, Runnable modification, PushOption push) {
		source.accept(new JavaModificationExpressionVisitor(context, module, javaWriter, this, modification, push));
	}

	@Override
	public Void visitPlatformSpecific(Expression expression) {
		if (expression instanceof JavaFunctionInterfaceCastExpression) {
			JavaFunctionInterfaceCastExpression jficExpression = (JavaFunctionInterfaceCastExpression) expression;
			if (jficExpression.value.type instanceof JavaFunctionalInterfaceTypeID) {
				jficExpression.value.accept(this);
			} else {
				visitFunctionalInterfaceWrapping(jficExpression);
			}
		} else {
			throw new AssertionError("Unrecognized platform expression: " + expression);
		}
		return null;
	}

	@Override
	public Void visitPostCall(PostCallExpression expression) {
		if (expression.member.getBuiltin() != null) {
			switch (expression.member.getBuiltin()) {
				case BYTE_INC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iAdd();
						javaWriter.constant(255);
						javaWriter.iAnd();
					}, PushOption.BEFORE);
					return null;
				case BYTE_DEC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iSub();
						javaWriter.constant(255);
						javaWriter.iAnd();
					}, PushOption.BEFORE);
					return null;
				case SBYTE_INC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iAdd();
						javaWriter.i2b();
					}, PushOption.BEFORE);
					return null;
				case SBYTE_DEC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iSub();
						javaWriter.i2b();
					}, PushOption.BEFORE);
					return null;
				case SHORT_INC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iAdd();
						javaWriter.i2s();
					}, PushOption.BEFORE);
					return null;
				case SHORT_DEC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iSub();
						javaWriter.i2s();
					}, PushOption.BEFORE);
					return null;
				case USHORT_INC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iAdd();
						javaWriter.constant(0xFFFF);
						javaWriter.iAnd();
					}, PushOption.BEFORE);
					return null;
				case USHORT_DEC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iSub();
						javaWriter.constant(0xFFFF);
						javaWriter.iAnd();
					}, PushOption.BEFORE);
					return null;
				case INT_INC:
				case UINT_INC:
				case USIZE_INC:
					if (expression.target instanceof GetLocalVariableExpression) {
						JavaLocalVariableInfo local = javaWriter.getLocalVariable(((GetLocalVariableExpression) expression.target).variable.variable);
						javaWriter.load(local);
						javaWriter.iinc(local.local);
					} else {
						modify(expression.target, () -> {
							javaWriter.iConst1();
							javaWriter.iAdd();
						}, PushOption.BEFORE);
					}
					return null;
				case INT_DEC:
				case UINT_DEC:
				case USIZE_DEC:
					if (expression.target instanceof GetLocalVariableExpression) {
						JavaLocalVariableInfo local = javaWriter.getLocalVariable(((GetLocalVariableExpression) expression.target).variable.variable);
						javaWriter.load(local);
						javaWriter.iinc(local.local, -1);
					} else {
						modify(expression.target, () -> {
							javaWriter.iConst1();
							javaWriter.iSub();
						}, PushOption.BEFORE);
					}
					return null;
				case LONG_INC:
				case ULONG_INC:
					modify(expression.target, () -> {
						javaWriter.constant(1L);
						javaWriter.lAdd();
					}, PushOption.BEFORE);
					return null;
				case LONG_DEC:
				case ULONG_DEC:
					modify(expression.target, () -> {
						javaWriter.constant(1L);
						javaWriter.lSub();
					}, PushOption.BEFORE);
					return null;
				case FLOAT_INC:
					modify(expression.target, () -> {
						javaWriter.constant(1f);
						javaWriter.fAdd();
					}, PushOption.BEFORE);
					return null;
				case FLOAT_DEC:
					modify(expression.target, () -> {
						javaWriter.constant(1f);
						javaWriter.fSub();
					}, PushOption.BEFORE);
					return null;
				case DOUBLE_INC:
					modify(expression.target, () -> {
						javaWriter.constant(1d);
						javaWriter.dAdd();
					}, PushOption.BEFORE);
					return null;
				case DOUBLE_DEC:
					modify(expression.target, () -> {
						javaWriter.constant(1d);
						javaWriter.dSub();
					}, PushOption.BEFORE);
					return null;
				default:
					throw new IllegalArgumentException("Unknown postcall builtin: " + expression.member.getBuiltin());
			}
		}

		modify(expression.target, () -> {
			if (!checkAndExecuteMethodInfo(expression.member, expression.type, expression))
				throw new IllegalStateException("Call target has no method info!");
		}, PushOption.BEFORE);

		return null;
	}

	@Override
	public Void visitRange(RangeExpression expression) {
		RangeTypeID type = (RangeTypeID) expression.type;
		Type cls = context.getType(expression.type);
		javaWriter.newObject(cls.getInternalName());
		javaWriter.dup();
		expression.from.accept(this);
		expression.to.accept(this);
		javaWriter.invokeSpecial(cls.getInternalName(), "<init>", "(" + context.getDescriptor(type.baseType) + context.getDescriptor(type.baseType) + ")V");

		return null;
	}


	@Override
	public Void visitSameObject(SameObjectExpression expression) {
		expression.left.accept(this);
		expression.right.accept(this);

		Label end = new Label();
		Label equal = new Label();

		if (expression.inverted)
			javaWriter.ifACmpNe(equal);
		else
			javaWriter.ifACmpEq(equal);

		javaWriter.iConst0();
		javaWriter.goTo(end);
		javaWriter.label(equal);
		javaWriter.iConst1();
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitSetField(SetFieldExpression expression) {
		expression.value.accept(this);
		expression.target.accept(this);
		javaWriter.dupX1(false, CompilerUtils.isLarge(expression.type));
		putField(expression.field);
		return null;
	}

	@Override
	public Void visitSetFunctionParameter(SetFunctionParameterExpression expression) {
		expression.value.accept(this);
		javaWriter.dup(CompilerUtils.isLarge(expression.value.type));
		JavaParameterInfo parameter = module.getParameterInfo(expression.parameter);
		javaWriter.store(context.getType(expression.type), parameter.index);
		return null;
	}

	@Override
	public Void visitSetLocalVariable(SetLocalVariableExpression expression) {
		expression.value.accept(this);
		Label label = new Label();
		javaWriter.label(label);
		final JavaLocalVariableInfo tag = javaWriter.getLocalVariable(expression.variable.variable);
		tag.end = label;

		javaWriter.dup(CompilerUtils.isLarge(expression.value.type));
		javaWriter.store(tag.type, tag.local);
		return null;
	}

	@Override
	public Void visitSetStaticField(SetStaticFieldExpression expression) {
		expression.value.accept(this);
		javaWriter.dup(CompilerUtils.isLarge(expression.value.type));
		javaWriter.putStaticField(context.getJavaField(expression.field));
		return null;
	}

	@Override
	public Void visitSetter(SetterExpression expression) {
		if (!checkAndExecuteMethodInfo(expression.setter, expression.type, expression)) {
			throw new IllegalStateException("Unknown Setter");
		}
		return null;
	}

	@Override
	public Void visitStaticGetter(StaticGetterExpression expression) {
		BuiltinID builtin = expression.getter.member.builtin;
		if (builtin == null) {
			if (context.hasJavaField(expression.getter)) {
				javaWriter.getStaticField(context.getJavaField(expression.getter));
				return null;
			}

			if (!checkAndExecuteMethodInfo(expression.getter, expression.type, expression))
				throw new IllegalStateException("Call target has no method info!");

			return null;
		}

		switch (builtin) {
			case BYTE_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case BYTE_GET_MAX_VALUE:
				javaWriter.constant(0xFF);
				break;
			case SBYTE_GET_MIN_VALUE:
				javaWriter.getStaticField(BYTE_MIN_VALUE);
				break;
			case SBYTE_GET_MAX_VALUE:
				javaWriter.getStaticField(BYTE_MAX_VALUE);
				break;
			case SHORT_GET_MIN_VALUE:
				javaWriter.getStaticField(SHORT_MIN_VALUE);
				break;
			case SHORT_GET_MAX_VALUE:
				javaWriter.getStaticField(SHORT_MAX_VALUE);
				break;
			case USHORT_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case USHORT_GET_MAX_VALUE:
				javaWriter.constant(0xFFFF);
				break;
			case INT_GET_MIN_VALUE:
				javaWriter.getStaticField(INTEGER_MIN_VALUE);
				break;
			case INT_GET_MAX_VALUE:
				javaWriter.getStaticField(INTEGER_MAX_VALUE);
				break;
			case UINT_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case UINT_GET_MAX_VALUE:
				javaWriter.constant(-1);
				break;
			case LONG_GET_MIN_VALUE:
				javaWriter.getStaticField(LONG_MIN_VALUE);
				break;
			case LONG_GET_MAX_VALUE:
				javaWriter.getStaticField(LONG_MAX_VALUE);
				break;
			case ULONG_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case ULONG_GET_MAX_VALUE:
				javaWriter.constant(-1L);
				break;
			case FLOAT_GET_MIN_VALUE:
				javaWriter.getStaticField(FLOAT_MIN_VALUE);
				break;
			case FLOAT_GET_MAX_VALUE:
				javaWriter.getStaticField(FLOAT_MAX_VALUE);
				break;
			case DOUBLE_GET_MIN_VALUE:
				javaWriter.getStaticField(DOUBLE_MIN_VALUE);
				break;
			case DOUBLE_GET_MAX_VALUE:
				javaWriter.getStaticField(DOUBLE_MAX_VALUE);
				break;
			case CHAR_GET_MIN_VALUE:
				javaWriter.getStaticField(CHARACTER_MIN_VALUE);
				break;
			case CHAR_GET_MAX_VALUE:
				javaWriter.getStaticField(CHARACTER_MAX_VALUE);
				break;
			case ENUM_VALUES: {
				DefinitionTypeID type = (DefinitionTypeID) ((ArrayTypeID) expression.type).elementType;
				JavaClass cls = context.getJavaClass(type.definition);
				javaWriter.invokeStatic(JavaNativeMethod.getNativeStatic(cls, "values", "()[L" + cls.internalName + ";"));
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown builtin: " + builtin);
		}

		throw new UnsupportedOperationException("Unknown builtin: " + builtin);
	}

	@Override
	public Void visitStaticSetter(StaticSetterExpression expression) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void visitFunctionalInterfaceWrapping(JavaFunctionInterfaceCastExpression expression) {
		final FunctionCastWrapperClass wrapper = generateFunctionCastWrapperClass(
				expression.position,
				(FunctionTypeID) expression.value.type,
				expression.functionType);

		expression.value.accept(this);
		javaWriter.newObject(wrapper.className);
		javaWriter.dupX1();
		javaWriter.swap();
		javaWriter.invokeSpecial(wrapper.className, "<init>", wrapper.constructorDesc);
	}

	private FunctionCastWrapperClass generateFunctionCastWrapperClass(CodePosition position, FunctionTypeID fromType, FunctionTypeID toType) {
		final String lambdaName = "lambda" + context.getLambdaCounter();
		final JavaClass classInfo = new JavaClass("zsynthetic", lambdaName, JavaClass.Kind.CLASS);
		final String className = "zsynthetic/" + lambdaName;

		String[] interfaces;
		String wrappedFromSignature = context.getDescriptor(fromType);
		String methodDescriptor;
		Type[] methodParameterTypes;
		JavaNativeMethod implementationMethod;
		if (toType instanceof JavaFunctionalInterfaceTypeID) {
			JavaNativeMethod javaMethod = ((JavaFunctionalInterfaceTypeID) toType).method;
			implementationMethod = new JavaNativeMethod(
					classInfo,
					JavaNativeMethod.Kind.COMPILED,
					javaMethod.name,
					true,
					javaMethod.descriptor,
					javaMethod.modifiers & ~JavaModifiers.ABSTRACT,
					javaMethod.genericResult,
					javaMethod.typeParameterArguments);

			final Method functionalInterfaceMethod = ((JavaFunctionalInterfaceTypeID) toType).functionalInterfaceMethod;

			methodDescriptor = Type.getMethodDescriptor(functionalInterfaceMethod);
			interfaces = new String[]{Type.getInternalName(functionalInterfaceMethod.getDeclaringClass())};

			final Class<?>[] methodParameterClasses = functionalInterfaceMethod.getParameterTypes();
			methodParameterTypes = new Type[methodParameterClasses.length];
			for (int i = 0; i < methodParameterClasses.length; i++) {
				final Class<?> methodParameterType = methodParameterClasses[i];
				methodParameterTypes[i] = Type.getType(methodParameterType);
			}
		} else {
			wrappedFromSignature = context.getMethodSignature(toType.header, true);
			methodDescriptor = context.getMethodDescriptor(toType.header);
			interfaces = new String[]{context.getInternalName(toType)};

			JavaSynthesizedFunctionInstance function = context.getFunction(toType);

			implementationMethod = new JavaNativeMethod(
					classInfo,
					JavaNativeMethod.Kind.COMPILED,
					function.getMethod(),
					true,
					methodDescriptor,
					JavaModifiers.PUBLIC,
					false // TODO: generic result or not
			);

			methodParameterTypes = new Type[toType.header.parameters.length];
			for (int i = 0; i < methodParameterTypes.length; i++) {
				methodParameterTypes[i] = context.getType(toType.header.parameters[i].type);
			}
		}

		final JavaNativeMethod wrappedMethod = context.getFunctionalInterface(fromType);
		final String constructorDesc = "(" + wrappedFromSignature + ")V";

		final ClassWriter lambdaCW = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
		lambdaCW.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", interfaces);

		//The field storing the wrapped object
		{
			lambdaCW.visitField(Modifier.PRIVATE | Modifier.FINAL, "wrapped", wrappedFromSignature, null, null).visitEnd();
		}

		//Constructor
		{
			final JavaWriter constructorWriter = new JavaWriter(context.logger, position, lambdaCW, JavaNativeMethod.getConstructor(javaWriter.method.cls, constructorDesc, Opcodes.ACC_PUBLIC), null, null, null);
			constructorWriter.start();
			constructorWriter.loadObject(0);
			constructorWriter.dup();
			constructorWriter.invokeSpecial(Object.class, "<init>", "()V");

			constructorWriter.loadObject(1);
			constructorWriter.putField(className, "wrapped", wrappedFromSignature);

			constructorWriter.ret();
			constructorWriter.end();
		}

		//The actual method
		{
			final JavaWriter functionWriter = new JavaWriter(context.logger, position, lambdaCW, implementationMethod, null, methodDescriptor, null, "java/lang/Override");
			functionWriter.start();

			//this.wrapped
			functionWriter.loadObject(0);
			functionWriter.getField(className, "wrapped", wrappedFromSignature);

			//Load all function parameters
			for (int i = 0; i < methodParameterTypes.length; i++) {
				functionWriter.load(methodParameterTypes[i], i + 1);
			}

			//Invokes the wrapped interface's method and returns the result
			functionWriter.invokeInterface(wrappedMethod);
			final TypeID returnType = fromType.header.getReturnType();
			final Type rtype = context.getType(returnType);
			if (!CompilerUtils.isPrimitive(returnType)) {
				functionWriter.checkCast(rtype);
			}
			functionWriter.returnType(rtype);
			functionWriter.end();
		}

		lambdaCW.visitEnd();
		context.register(className, lambdaCW.toByteArray());

		return new FunctionCastWrapperClass(className, constructorDesc);
	}

	@Override
	public Void visitSupertypeCast(SupertypeCastExpression expression) {
		expression.value.accept(this);
		return null; // nothing to do
	}

	@Override
	public Void visitSubtypeCast(SubtypeCastExpression expression) {
		expression.value.accept(this);
		javaWriter.checkCast(context.getType(expression.type));
		return null; // nothing to do
	}

	@Override
	public Void visitThis(ThisExpression expression) {
		javaWriter.load(context.getType(expression.type), 0);
		return null;
	}

	@Override
	public Void visitThrow(ThrowExpression expression) {
		expression.value.accept(this);
		javaWriter.aThrow();
		return null;
	}

	@Override
	public Void visitTryConvert(TryConvertExpression expression) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Void visitTryRethrowAsException(TryRethrowAsExceptionExpression expression) {
		expression.value.accept(this);
		javaWriter.dup();
		//FIXME better way of finding the error
		final String internalName = context.getInternalName(expression.value.type) + "$Error";
		javaWriter.instanceOf(internalName);
		final Label end = new Label();
		javaWriter.ifNE(end);
		javaWriter.newObject(Type.getInternalName(Exception.class));
		javaWriter.dup();
		javaWriter.invokeSpecial(Type.getInternalName(Exception.class), "<init>", "()V");
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitTryRethrowAsResult(TryRethrowAsResultExpression expression) {
		expression.value.accept(this);
		javaWriter.dup();
		//FIXME better way of finding the error
		final String internalName = context.getInternalName(expression.value.type) + "$Error";
		javaWriter.instanceOf(internalName);
		final Label end = new Label();
		javaWriter.ifNE(end);
		javaWriter.newObject(internalName);
		javaWriter.dupX1();
		javaWriter.swap();
		javaWriter.invokeSpecial(internalName, "<init>", "(Ljava/lang/Object;)V");
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitUnary(UnaryExpression expression) {
		switch (expression.operator) {
			case INT_HIGHEST_ONE_BIT:
			case UINT_HIGHEST_ONE_BIT:
			case USIZE_HIGHEST_ONE_BIT:
				javaWriter.invokeStatic(INTEGER_HIGHEST_ONE_BIT);
				break;
			case INT_LOWEST_ONE_BIT:
			case UINT_LOWEST_ONE_BIT:
			case USIZE_LOWEST_ONE_BIT:
				javaWriter.invokeStatic(INTEGER_LOWEST_ONE_BIT);
				break;
			case INT_HIGHEST_ZERO_BIT:
			case UINT_HIGHEST_ZERO_BIT:
			case USIZE_HIGHEST_ZERO_BIT:
				javaWriter.iNeg();
				javaWriter.invokeStatic(INTEGER_HIGHEST_ONE_BIT);
				break;
			case INT_LOWEST_ZERO_BIT:
			case UINT_LOWEST_ZERO_BIT:
			case USIZE_LOWEST_ZERO_BIT:
				javaWriter.iNeg();
				javaWriter.invokeStatic(INTEGER_LOWEST_ONE_BIT);
				break;
			case INT_BIT_COUNT:
			case UINT_BIT_COUNT:
			case USIZE_BIT_COUNT:
				javaWriter.invokeStatic(INTEGER_BIT_COUNT);
				break;
			case LONG_HIGHEST_ONE_BIT:
			case ULONG_HIGHEST_ONE_BIT:
				javaWriter.invokeStatic(LONG_HIGHEST_ONE_BIT);
				break;
			case LONG_LOWEST_ONE_BIT:
			case ULONG_LOWEST_ONE_BIT:
				javaWriter.invokeStatic(LONG_LOWEST_ONE_BIT);
				break;
			case LONG_HIGHEST_ZERO_BIT:
			case ULONG_HIGHEST_ZERO_BIT:
				javaWriter.lNeg();
				javaWriter.invokeStatic(LONG_HIGHEST_ONE_BIT);
				break;
			case LONG_LOWEST_ZERO_BIT:
			case ULONG_LOWEST_ZERO_BIT:
				javaWriter.lNeg();
				javaWriter.invokeStatic(LONG_LOWEST_ONE_BIT);
				break;
			case LONG_BIT_COUNT:
			case ULONG_BIT_COUNT:
				javaWriter.invokeStatic(LONG_BIT_COUNT);
				break;
			case FLOAT_BITS:
				javaWriter.invokeStatic(FLOAT_BITS);
				break;
			case DOUBLE_BITS:
				javaWriter.invokeStatic(DOUBLE_BITS);
				break;
			case STRING_LENGTH:
				javaWriter.invokeVirtual(STRING_LENGTH);
				break;
			case STRING_CHARACTERS:
				javaWriter.invokeVirtual(STRING_CHARACTERS);
				break;
			case STRING_ISEMPTY:
				javaWriter.invokeVirtual(STRING_ISEMPTY);
				break;
			case ASSOC_SIZE:
				javaWriter.invokeVirtual(MAP_SIZE);
				break;
			case ASSOC_ISEMPTY:
				javaWriter.invokeVirtual(MAP_ISEMPTY);
				break;
			case ASSOC_KEYS: {
				Type resultType = context.getType(expression.type);

				javaWriter.invokeVirtual(MAP_KEYS);
				javaWriter.dup();
				javaWriter.invokeVirtual(COLLECTION_SIZE);
				javaWriter.newArray(resultType);
				javaWriter.invokeVirtual(COLLECTION_TOARRAY);
				javaWriter.checkCast(resultType);
				break;
			}
			case ASSOC_VALUES: {
				Type resultType = context.getType(expression.type);

				javaWriter.invokeVirtual(MAP_VALUES);
				javaWriter.dup();
				javaWriter.invokeVirtual(COLLECTION_SIZE);
				javaWriter.newArray(resultType);
				javaWriter.invokeVirtual(COLLECTION_TOARRAY);
				javaWriter.checkCast(resultType);
				break;
			}
			case ARRAY_LENGTH:
				javaWriter.arrayLength();
				break;
			case ARRAY_HASHCODE: {
				ArrayTypeID type = (ArrayTypeID) expression.target.type;
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
			case ARRAY_ISEMPTY:
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
			case ENUM_NAME:
				javaWriter.invokeVirtual(ENUM_NAME);
				break;
			case ENUM_ORDINAL:
				javaWriter.invokeVirtual(ENUM_ORDINAL);
				break;
			case RANGE_FROM: {
				RangeTypeID type = (RangeTypeID) expression.target.type;
				Type jType = context.getType(expression.target.type);
				javaWriter.getField(jType.getInternalName(), "from", context.getDescriptor(type.baseType));
				break;
			}
			case RANGE_TO:
				RangeTypeID type = (RangeTypeID) expression.target.type;
				Type jType = context.getType(expression.target.type);
				javaWriter.getField(jType.getInternalName(), "to", context.getDescriptor(type.baseType));
				break;
			default:
				throw new IllegalArgumentException("Unknown unary operator: " + expression.operator);
		}
		return null;
	}

	@Override
	public Void visitVariantValue(VariantValueExpression expression) {
		JavaVariantOption tag = context.getJavaVariantOption(expression.option);
		final String internalName = tag.variantOptionClass.internalName;
		javaWriter.newObject(internalName);
		javaWriter.dup();

		for (Expression argument : expression.arguments) {
			argument.accept(this);
		}

		final StringBuilder builder = new StringBuilder("(");
		for (TypeID type : expression.option.getOption().types) {
			builder.append(context.getDescriptor(type));
		}
		builder.append(")V");


		javaWriter.invokeSpecial(internalName, "<init>", builder.toString());
		return null;
	}

	@Override
	public Void visitWrapOptional(WrapOptionalExpression expression) {
		//Does nothing if not required to be wrapped
		expression.value.accept(this);
		expression.value.type.accept(expression.value.type, boxingTypeVisitor);
		return null;
	}

	public JavaWriter getJavaWriter() {
		return javaWriter;
	}

	//Will return true if a JavaMethodInfo.class tag exists, and will compile that tag
	@SuppressWarnings({"Raw"})
	boolean checkAndExecuteMethodInfo(DefinitionMemberRef member, TypeID resultType, Expression expression) {
		JavaNativeMethod methodInfo = context.getJavaMethod(member);
		if (methodInfo == null)
			return false;

		executeMethodInfo(resultType, expression, methodInfo);
		return true;
	}

	@SuppressWarnings({"Raw", "unchecked"})
	private void executeMethodInfo(TypeID resultType, Expression expression, JavaNativeMethod methodInfo) {
		if (methodInfo.kind == JavaNativeMethod.Kind.STATIC) {
			getJavaWriter().invokeStatic(methodInfo);
		} else if (methodInfo.kind == JavaNativeMethod.Kind.INTERFACE) {
			getJavaWriter().invokeInterface(methodInfo);
		} else if (methodInfo.kind == JavaNativeMethod.Kind.EXPANSION) {
			getJavaWriter().invokeStatic(methodInfo);
		} else if (methodInfo.cls != null && methodInfo.cls.kind == JavaClass.Kind.INTERFACE) {
			getJavaWriter().invokeInterface(methodInfo);
		} else {
			getJavaWriter().invokeVirtual(methodInfo);
		}
		if (methodInfo.genericResult) {
			handleGenericReturnValue(resultType);
		}

		//Make sure that method results are popped if ZC thinks its a void but it actually is not.
		//Fixes an issue for List#add() returning void in ZC but Z in Java.
		if (resultType == BasicTypeID.VOID && !methodInfo.descriptor.equals("") && !methodInfo.descriptor.endsWith(")V")) {
			final boolean isLarge = methodInfo.descriptor.endsWith(")D") && methodInfo.descriptor.endsWith(")J");
			getJavaWriter().pop(isLarge);
		}
	}

	//Will return true if a JavaFieldInfo.class tag exists, and will compile that tag
	public void putField(FieldInstance field) {
		JavaField fieldInfo = context.getJavaField(field);
		if (field.getModifiers().isStatic()) {
			getJavaWriter().putStaticField(fieldInfo);
		} else {
			getJavaWriter().putField(fieldInfo);
		}
	}

	public void getField(FieldInstance field) {
		final JavaField fieldInfo = context.getJavaField(field);
		if (field.getModifiers().isStatic()) {
			getJavaWriter().getStaticField(fieldInfo);
		} else {
			getJavaWriter().getField(fieldInfo);
		}
	}

	private static class FunctionCastWrapperClass {
		final String className;
		final String constructorDesc;

		FunctionCastWrapperClass(String className, String constructorDesc) {
			this.className = className;
			this.constructorDesc = constructorDesc;
		}
	}
}
