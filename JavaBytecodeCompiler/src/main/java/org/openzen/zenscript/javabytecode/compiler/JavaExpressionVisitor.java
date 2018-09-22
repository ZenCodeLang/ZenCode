package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.javashared.JavaParameterInfo;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringJoiner;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.member.ref.ConstMemberRef;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.FieldMemberRef;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.javabytecode.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;
import org.openzen.zenscript.codemodel.type.storage.SharedStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.UniqueStorageTag;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaSynthesizedClass;
import org.openzen.zenscript.javashared.JavaVariantOption;

public class JavaExpressionVisitor implements ExpressionVisitor<Void> {
	private static final JavaMethod BOOLEAN_PARSE = JavaMethod.getNativeStatic(JavaClass.BOOLEAN, "parseBoolean", "(Ljava/lang/String;)Z");
	private static final JavaMethod BOOLEAN_TO_STRING = JavaMethod.getNativeStatic(JavaClass.BOOLEAN, "toString", "(Z)Ljava/lang/String;");
	private static final JavaMethod BYTE_PARSE = JavaMethod.getNativeStatic(JavaClass.BYTE, "parseByte", "(Ljava/lang/String;)B");
	private static final JavaMethod BYTE_PARSE_WITH_BASE = JavaMethod.getNativeStatic(JavaClass.BYTE, "parseByte", "(Ljava/lang/String;I)B");
	private static final JavaField BYTE_MIN_VALUE = new JavaField(JavaClass.BYTE, "MIN_VALUE", "B");
	private static final JavaField BYTE_MAX_VALUE = new JavaField(JavaClass.BYTE, "MAX_VALUE", "B");
	private static final JavaMethod BYTE_TO_STRING = JavaMethod.getNativeStatic(JavaClass.BYTE, "toString", "(B)Ljava/lang/String;");
	private static final JavaMethod SHORT_PARSE = JavaMethod.getNativeStatic(JavaClass.SHORT, "parseShort", "(Ljava/lang/String;)S");
	private static final JavaMethod SHORT_PARSE_WITH_BASE = JavaMethod.getNativeStatic(JavaClass.SHORT, "parseShort", "(Ljava/lang/String;I)S");
	private static final JavaField SHORT_MIN_VALUE = new JavaField(JavaClass.SHORT, "MIN_VALUE", "S");
	private static final JavaField SHORT_MAX_VALUE = new JavaField(JavaClass.SHORT, "MAX_VALUE", "S");
	private static final JavaMethod SHORT_TO_STRING = JavaMethod.getNativeStatic(JavaClass.SHORT, "toString", "(S)Ljava/lang/String;");
	private static final JavaMethod INTEGER_COMPARE_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.INTEGER, "compareUnsigned", "(II)I");
	private static final JavaMethod INTEGER_DIVIDE_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.INTEGER, "divideUnsigned", "(II)I");
	private static final JavaMethod INTEGER_REMAINDER_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.INTEGER, "remainderUnsigned", "(II)I");
	private static final JavaMethod INTEGER_NUMBER_OF_TRAILING_ZEROS = JavaMethod.getNativeStatic(JavaClass.INTEGER, "numberOfTrailingZeros", "(I)I");
	private static final JavaMethod INTEGER_NUMBER_OF_LEADING_ZEROS = JavaMethod.getNativeStatic(JavaClass.INTEGER, "numberOfLeadingZeros", "(I)I");
	private static final JavaMethod INTEGER_PARSE = JavaMethod.getNativeStatic(JavaClass.INTEGER, "parseInt", "(Ljava/lang/String;)I");
	private static final JavaMethod INTEGER_PARSE_WITH_BASE = JavaMethod.getNativeStatic(JavaClass.INTEGER, "parseInt", "(Ljava/lang/String;I)I");
	private static final JavaMethod INTEGER_PARSE_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.INTEGER, "parseUnsignedInt", "(Ljava/lang/String;)I");
	private static final JavaMethod INTEGER_PARSE_UNSIGNED_WITH_BASE = JavaMethod.getNativeStatic(JavaClass.INTEGER, "parseUnsignedInt", "(Ljava/lang/String;I)I");
	private static final JavaMethod INTEGER_HIGHEST_ONE_BIT = JavaMethod.getNativeStatic(JavaClass.INTEGER, "highestOneBit", "(I)I");
	private static final JavaMethod INTEGER_LOWEST_ONE_BIT = JavaMethod.getNativeStatic(JavaClass.INTEGER, "lowestOneBit", "(I)I");
	private static final JavaMethod INTEGER_BIT_COUNT = JavaMethod.getNativeStatic(JavaClass.INTEGER, "bitCount", "(I)I");
	private static final JavaField INTEGER_MIN_VALUE = new JavaField(JavaClass.INTEGER, "MIN_VALUE", "I");
	private static final JavaField INTEGER_MAX_VALUE = new JavaField(JavaClass.INTEGER, "MAX_VALUE", "I");
	private static final JavaMethod INTEGER_TO_STRING = JavaMethod.getNativeStatic(JavaClass.INTEGER, "toString", "(I)Ljava/lang/String;");
	private static final JavaMethod INTEGER_TO_UNSIGNED_STRING = JavaMethod.getNativeStatic(JavaClass.INTEGER, "toUnsignedString", "(I)Ljava/lang/String;");
	private static final JavaMethod LONG_COMPARE = JavaMethod.getNativeStatic(JavaClass.LONG, "compare", "(JJ)I");
	private static final JavaMethod LONG_COMPARE_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.LONG, "compareUnsigned", "(JJ)I");
	private static final JavaMethod LONG_DIVIDE_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.LONG, "divideUnsigned", "(JJ)J");
	private static final JavaMethod LONG_REMAINDER_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.LONG, "remainderUnsigned", "(JJ)J");
	private static final JavaMethod LONG_NUMBER_OF_TRAILING_ZEROS = JavaMethod.getNativeStatic(JavaClass.LONG, "numberOfTrailingZeros", "(J)I");
	private static final JavaMethod LONG_NUMBER_OF_LEADING_ZEROS = JavaMethod.getNativeStatic(JavaClass.LONG, "numberOfLeadingZeros", "(J)I");
	private static final JavaMethod LONG_PARSE = JavaMethod.getNativeStatic(JavaClass.LONG, "parseLong", "(Ljava/lang/String;)J");
	private static final JavaMethod LONG_PARSE_WITH_BASE = JavaMethod.getNativeStatic(JavaClass.LONG, "parseLong", "(Ljava/lang/String;I)J");
	private static final JavaMethod LONG_PARSE_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.LONG, "parseUnsignedLong", "(Ljava/lang/String;)J");
	private static final JavaMethod LONG_PARSE_UNSIGNED_WITH_BASE = JavaMethod.getNativeStatic(JavaClass.LONG, "parseUnsignedLong", "(Ljava/lang/String;I)J");
	private static final JavaMethod LONG_HIGHEST_ONE_BIT = JavaMethod.getNativeStatic(JavaClass.LONG, "highestOneBit", "(J)J");
	private static final JavaMethod LONG_LOWEST_ONE_BIT = JavaMethod.getNativeStatic(JavaClass.LONG, "lowestOneBit", "(J)J");
	private static final JavaMethod LONG_BIT_COUNT = JavaMethod.getNativeStatic(JavaClass.LONG, "bitCount", "(J)I");
	private static final JavaField LONG_MIN_VALUE = new JavaField(JavaClass.LONG, "MIN_VALUE", "J");
	private static final JavaField LONG_MAX_VALUE = new JavaField(JavaClass.LONG, "MAX_VALUE", "J");
	private static final JavaMethod LONG_TO_STRING = JavaMethod.getNativeStatic(JavaClass.LONG, "toString", "(J)Ljava/lang/String;");
	private static final JavaMethod LONG_TO_UNSIGNED_STRING = JavaMethod.getNativeStatic(JavaClass.LONG, "toUnsignedString", "(J)Ljava/lang/String;");
	private static final JavaMethod FLOAT_COMPARE = JavaMethod.getNativeStatic(JavaClass.FLOAT, "compare", "(FF)I");
	private static final JavaMethod FLOAT_PARSE = JavaMethod.getNativeStatic(JavaClass.FLOAT, "parseFloat", "(Ljava/lang/String;)F");
	private static final JavaMethod FLOAT_FROM_BITS = JavaMethod.getNativeStatic(JavaClass.FLOAT, "intBitsToFloat", "(I)F");
	private static final JavaMethod FLOAT_BITS = JavaMethod.getNativeStatic(JavaClass.FLOAT, "floatToRawIntBits", "(F)I");
	private static final JavaField FLOAT_MIN_VALUE = new JavaField(JavaClass.FLOAT, "MIN_VALUE", "F");
	private static final JavaField FLOAT_MAX_VALUE = new JavaField(JavaClass.FLOAT, "MAX_VALUE", "F");
	private static final JavaMethod FLOAT_TO_STRING = JavaMethod.getNativeStatic(JavaClass.FLOAT, "toString", "(F)Ljava/lang/String;");
	private static final JavaMethod DOUBLE_COMPARE = JavaMethod.getNativeStatic(JavaClass.DOUBLE, "compare", "(DD)I");
	private static final JavaMethod DOUBLE_PARSE = JavaMethod.getNativeStatic(JavaClass.DOUBLE, "parseDouble", "(Ljava/lang/String;)D");
	private static final JavaMethod DOUBLE_FROM_BITS = JavaMethod.getNativeStatic(JavaClass.DOUBLE, "longBitsToDouble", "(J)D");
	private static final JavaMethod DOUBLE_BITS = JavaMethod.getNativeStatic(JavaClass.DOUBLE, "doubleToRawLongBits", "(D)J");
	private static final JavaField DOUBLE_MIN_VALUE = new JavaField(JavaClass.DOUBLE, "MIN_VALUE", "D");
	private static final JavaField DOUBLE_MAX_VALUE = new JavaField(JavaClass.DOUBLE, "MAX_VALUE", "D");
	private static final JavaMethod DOUBLE_TO_STRING = JavaMethod.getNativeStatic(JavaClass.DOUBLE, "toString", "(D)Ljava/lang/String;");
	private static final JavaMethod CHARACTER_TO_LOWER_CASE = JavaMethod.getNativeVirtual(JavaClass.CHARACTER, "toLowerCase", "()C");
	private static final JavaMethod CHARACTER_TO_UPPER_CASE = JavaMethod.getNativeVirtual(JavaClass.CHARACTER, "toUpperCase", "()C");
	private static final JavaField CHARACTER_MIN_VALUE = new JavaField(JavaClass.CHARACTER, "MIN_VALUE", "C");
	private static final JavaField CHARACTER_MAX_VALUE = new JavaField(JavaClass.CHARACTER, "MAX_VALUE", "C");
	private static final JavaMethod CHARACTER_TO_STRING = JavaMethod.getNativeStatic(JavaClass.CHARACTER, "toString", "(C)Ljava/lang/String;");
	private static final JavaMethod STRING_COMPARETO = JavaMethod.getNativeVirtual(JavaClass.STRING, "compareTo", "(Ljava/lang/String;)I");
	private static final JavaMethod STRING_CONCAT = JavaMethod.getNativeVirtual(JavaClass.STRING, "concat", "(Ljava/lang/String;)Ljava/lang/String;");
	private static final JavaMethod STRING_CHAR_AT = JavaMethod.getNativeVirtual(JavaClass.STRING, "charAt", "(I)C");
	private static final JavaMethod STRING_SUBSTRING = JavaMethod.getNativeVirtual(JavaClass.STRING, "substring", "(II)Ljava/lang/String;");
	private static final JavaMethod STRING_TRIM = JavaMethod.getNativeVirtual(JavaClass.STRING, "trim", "()Ljava/lang/String;");
	private static final JavaMethod STRING_TO_LOWER_CASE = JavaMethod.getNativeVirtual(JavaClass.STRING, "toLowerCase", "()Ljava/lang/String;");
	private static final JavaMethod STRING_TO_UPPER_CASE = JavaMethod.getNativeVirtual(JavaClass.STRING, "toUpperCase", "()Ljava/lang/String;");
	private static final JavaMethod STRING_LENGTH = JavaMethod.getNativeVirtual(JavaClass.STRING, "length", "()I");
	private static final JavaMethod STRING_CHARACTERS = JavaMethod.getNativeVirtual(JavaClass.STRING, "toCharArray", "()[C");
	private static final JavaMethod STRING_ISEMPTY = JavaMethod.getNativeVirtual(JavaClass.STRING, "isEmpty", "()Z");
	private static final JavaMethod ENUM_COMPARETO = JavaMethod.getNativeVirtual(JavaClass.ENUM, "compareTo", "(Ljava/lang/Enum;)I");
	private static final JavaMethod ENUM_NAME = JavaMethod.getNativeVirtual(JavaClass.ENUM, "name", "()Ljava/lang/String;");
	private static final JavaMethod ENUM_ORDINAL = JavaMethod.getNativeVirtual(JavaClass.ENUM, "ordinal", "()I");
	private static final JavaMethod MAP_GET = JavaMethod.getNativeVirtual(JavaClass.MAP, "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
	private static final JavaMethod MAP_PUT = JavaMethod.getNativeVirtual(JavaClass.MAP, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
	private static final JavaMethod MAP_CONTAINS_KEY = JavaMethod.getNativeVirtual(JavaClass.MAP, "containsKey", "(Ljava/lang/Object;)Z");
	private static final JavaMethod MAP_SIZE = JavaMethod.getNativeVirtual(JavaClass.MAP, "size", "()I");
	private static final JavaMethod MAP_ISEMPTY = JavaMethod.getNativeVirtual(JavaClass.MAP, "isEmpty", "()Z");
	private static final JavaMethod MAP_KEYS = JavaMethod.getNativeVirtual(JavaClass.MAP, "keys", "()Ljava/lang/Object;");
	private static final JavaMethod MAP_VALUES = JavaMethod.getNativeVirtual(JavaClass.MAP, "values", "()Ljava/lang/Object;");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_OBJECTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([Ljava/lang/Object;II)[Ljava/lang/Object;");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_BOOLS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([ZII)[Z");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_BYTES = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([BII)[B");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_SHORTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([SII)[S");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_INTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([III)[I");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_LONGS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([JII)[J");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_FLOATS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([FII)[F");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_DOUBLES = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([DII)[D");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_CHARS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([CII)[C");
	private static final JavaMethod ARRAYS_EQUALS_OBJECTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([Ljava/lang/Object[Ljava/lang/Object)Z");
	private static final JavaMethod ARRAYS_EQUALS_BOOLS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([Z[Z)Z");
	private static final JavaMethod ARRAYS_EQUALS_BYTES = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([B[B)Z");
	private static final JavaMethod ARRAYS_EQUALS_SHORTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([S[S)Z");
	private static final JavaMethod ARRAYS_EQUALS_INTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([I[I)Z");
	private static final JavaMethod ARRAYS_EQUALS_LONGS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([L[L)Z");
	private static final JavaMethod ARRAYS_EQUALS_FLOATS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([F[F)Z");
	private static final JavaMethod ARRAYS_EQUALS_DOUBLES = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([D[D)Z");
	private static final JavaMethod ARRAYS_EQUALS_CHARS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([C[C)Z");
	private static final JavaMethod ARRAYS_DEEPHASHCODE = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "deepHashCode", "([Ljava/lang/Object;)");
	private static final JavaMethod ARRAYS_HASHCODE_BOOLS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([Z)I");
	private static final JavaMethod ARRAYS_HASHCODE_BYTES = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([B)I");
	private static final JavaMethod ARRAYS_HASHCODE_SHORTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([S)I");
	private static final JavaMethod ARRAYS_HASHCODE_INTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([I)I");
	private static final JavaMethod ARRAYS_HASHCODE_LONGS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([L)I");
	private static final JavaMethod ARRAYS_HASHCODE_FLOATS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([F)I");
	private static final JavaMethod ARRAYS_HASHCODE_DOUBLES = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([D)I");
	private static final JavaMethod ARRAYS_HASHCODE_CHARS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([C)I");
	public static final JavaMethod OBJECT_HASHCODE = JavaMethod.getNativeVirtual(JavaClass.OBJECT, "hashCode", "()I");
	private static final JavaMethod COLLECTION_SIZE = JavaMethod.getNativeVirtual(JavaClass.COLLECTION, "size", "()I");
	private static final JavaMethod COLLECTION_TOARRAY = JavaMethod.getNativeVirtual(JavaClass.COLLECTION, "toArray", "([Ljava/lang/Object;)[Ljava/lang/Object;");
	
	private static final JavaMethod SHARED_INIT = JavaMethod.getConstructor(JavaClass.SHARED, "(Ljava/lang/Object;)V", Modifier.PUBLIC);
	private static final JavaMethod SHARED_GET = JavaMethod.getNativeVirtual(JavaClass.SHARED, "get", "()Ljava/lang/Object;");
	private static final JavaMethod SHARED_ADDREF = JavaMethod.getNativeVirtual(JavaClass.SHARED, "addRef", "()V");
	private static final JavaMethod SHARED_RELEASE = JavaMethod.getNativeVirtual(JavaClass.SHARED, "release", "()V");
	
	protected final JavaWriter javaWriter;
	private final JavaCapturedExpressionVisitor capturedExpressionVisitor = new JavaCapturedExpressionVisitor(this);
	private final JavaBytecodeContext context;

    public JavaExpressionVisitor(JavaBytecodeContext context, JavaWriter javaWriter) {
        this.javaWriter = javaWriter;
		this.context = context;
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
        Type type = context.getType(((ArrayTypeID)expression.type).elementType);
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
		if (expression.operator.getBuiltin() != null) {
			switch (expression.operator.getBuiltin()) {
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
					throw new UnsupportedOperationException("Unknown builtin comparator: " + expression.operator.getBuiltin());
			}
		} else {
			if (!checkAndExecuteMethodInfo(expression.operator, expression.type))
				throw new IllegalStateException("Call target has no method info!");

			expression.left.accept(this);
			expression.right.accept(this);
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
		BuiltinID builtin = expression.member.getBuiltin();
		if (builtin == null) {
			expression.target.accept(this);
			for (Expression argument : expression.arguments.arguments) {
				argument.accept(this);
			}

			if (!checkAndExecuteMethodInfo(expression.member, expression.type))
				throw new IllegalStateException("Call target has no method info!");
			//if (expression.member.getHeader().returnType != expression.type)

			//TODO see if the types differ (e.g. if a generic method was invoked) and only cast then
			if(expression.type != BasicTypeID.VOID)
				javaWriter.checkCast(context.getInternalName(expression.type));
			return null;
		}

		switch (builtin) {
			case STRING_RANGEGET:
			case ARRAY_INDEXGETRANGE:
				break;
			default:
				expression.target.accept(this);
				for (Expression argument : expression.arguments.arguments) {
					argument.accept(this);
				}
		}

		switch (builtin) {
			case BOOL_NOT:
				javaWriter.iConst1();
				javaWriter.iXor();
				break;
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
			case BYTE_NOT:
			case SBYTE_NOT:
			case SHORT_NOT:
			case USHORT_NOT:
			case INT_NOT:
			case UINT_NOT:
			case USIZE_NOT:
				javaWriter.iNot();
				break;
			case SBYTE_NEG:
			case SHORT_NEG:
			case INT_NEG:
				javaWriter.iNeg();
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
			case BYTE_DIV_BYTE:
			case USHORT_DIV_USHORT:
			case UINT_DIV_UINT:
				javaWriter.invokeStatic(INTEGER_DIVIDE_UNSIGNED);
				break;
			case BYTE_MOD_BYTE:
			case USHORT_MOD_USHORT:
			case UINT_MOD_UINT:
				javaWriter.invokeStatic(INTEGER_REMAINDER_UNSIGNED);
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
			case SBYTE_USHR:
			case USHORT_SHR:
			case SHORT_USHR:
			case INT_USHR:
			case UINT_SHR:
			case USIZE_SHR:
				javaWriter.iUShr();
				break;
			case INT_COUNT_LOW_ZEROES:
			case UINT_COUNT_LOW_ZEROES:
			case USIZE_COUNT_LOW_ZEROES:
				javaWriter.invokeStatic(INTEGER_NUMBER_OF_TRAILING_ZEROS);
				break;
			case INT_COUNT_HIGH_ZEROES:
			case UINT_COUNT_HIGH_ZEROES:
			case USIZE_COUNT_HIGH_ZEROES:
				javaWriter.invokeStatic(INTEGER_NUMBER_OF_LEADING_ZEROS);
				break;
			case INT_COUNT_LOW_ONES:
			case UINT_COUNT_LOW_ONES:
			case USIZE_COUNT_LOW_ONES:
				javaWriter.iNot();
				javaWriter.invokeStatic(INTEGER_NUMBER_OF_TRAILING_ZEROS);
				break;
			case INT_COUNT_HIGH_ONES:
			case UINT_COUNT_HIGH_ONES:
			case USIZE_COUNT_HIGH_ONES:
				javaWriter.iNot();
				javaWriter.invokeStatic(INTEGER_NUMBER_OF_LEADING_ZEROS);
				break;
			case LONG_NOT:
			case ULONG_NOT:
				javaWriter.lNot();
				break;
			case LONG_NEG:
				javaWriter.lNeg();
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
			case LONG_USHR:
			case ULONG_SHR:
				javaWriter.lUShr();
				break;
			case LONG_COUNT_LOW_ZEROES:
			case ULONG_COUNT_LOW_ZEROES:
				javaWriter.invokeStatic(LONG_NUMBER_OF_TRAILING_ZEROS);
				break;
			case LONG_COUNT_HIGH_ZEROES:
			case ULONG_COUNT_HIGH_ZEROES:
				javaWriter.invokeStatic(LONG_NUMBER_OF_LEADING_ZEROS);
				break;
			case LONG_COUNT_LOW_ONES:
			case ULONG_COUNT_LOW_ONES:
				javaWriter.lNot();
				javaWriter.invokeStatic(LONG_NUMBER_OF_TRAILING_ZEROS);
				break;
			case LONG_COUNT_HIGH_ONES:
			case ULONG_COUNT_HIGH_ONES:
				javaWriter.lNot();
				javaWriter.invokeStatic(LONG_NUMBER_OF_LEADING_ZEROS);
				break;
			case ULONG_DIV_ULONG:
				javaWriter.invokeStatic(LONG_DIVIDE_UNSIGNED);
				break;
			case ULONG_MOD_ULONG:
				javaWriter.invokeStatic(LONG_REMAINDER_UNSIGNED);
				break;
			case FLOAT_NEG:
				javaWriter.fNeg();
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
			case DOUBLE_NEG:
				javaWriter.dNeg();
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
			case CHAR_ADD_INT:
				javaWriter.iAdd();
				break;
			case CHAR_SUB_INT:
			case CHAR_SUB_CHAR:
				javaWriter.iSub();
				break;
			case CHAR_REMOVE_DIACRITICS:
				throw new UnsupportedOperationException("Not yet supported!");
			case CHAR_TO_LOWER_CASE:
				javaWriter.invokeStatic(CHARACTER_TO_LOWER_CASE);
				break;
			case CHAR_TO_UPPER_CASE:
				javaWriter.invokeStatic(CHARACTER_TO_UPPER_CASE);
				break;
			case STRING_ADD_STRING:
				javaWriter.invokeVirtual(STRING_CONCAT);
				break;
			case STRING_INDEXGET:
				javaWriter.invokeVirtual(STRING_CHAR_AT);
				break;
			case STRING_RANGEGET: {
				expression.target.accept(this);
				Expression argument = expression.arguments.arguments[0];
				if (argument instanceof RangeExpression) {
					RangeExpression rangeArgument = (RangeExpression) argument;
					rangeArgument.from.accept(this);
					rangeArgument.to.accept(this); // TODO: is this string.length ? if so, use the other substring method
				} else {
					argument.accept(this);
					javaWriter.dup();
					int tmp = javaWriter.local(Type.getType("zsynthetic/IntRange"));
					javaWriter.storeInt(tmp);
					javaWriter.getField("zsynthetic/IntRange", "from", "I");
					javaWriter.loadInt(tmp);
					javaWriter.getField("zsynthetic/IntRange", "to", "I");
				}
				javaWriter.invokeVirtual(STRING_SUBSTRING);
				break;
			}
			case STRING_REMOVE_DIACRITICS:
				throw new UnsupportedOperationException("Not yet supported!");
			case STRING_TRIM:
				javaWriter.invokeVirtual(STRING_TRIM);
				break;
			case STRING_TO_LOWER_CASE:
				javaWriter.invokeVirtual(STRING_TO_LOWER_CASE);
				break;
			case STRING_TO_UPPER_CASE:
				javaWriter.invokeVirtual(STRING_TO_UPPER_CASE);
				break;
			case ASSOC_INDEXGET:
			case ASSOC_GETORDEFAULT: {
				javaWriter.invokeVirtual(MAP_GET);

				AssocTypeID type = (AssocTypeID) expression.target.type;
				javaWriter.checkCast(context.getInternalName(type.valueType));
				break;
			}
			case ASSOC_INDEXSET:
				javaWriter.invokeVirtual(MAP_PUT);
				javaWriter.pop();
				break;
			case ASSOC_CONTAINS:
				javaWriter.invokeVirtual(MAP_CONTAINS_KEY);
				break;
			case ASSOC_EQUALS:
				throw new UnsupportedOperationException("Not yet supported!");
			case ASSOC_NOTEQUALS:
				throw new UnsupportedOperationException("Not yet supported!");
			case ASSOC_SAME:
			case GENERICMAP_SAME:
			case ARRAY_SAME:
			case FUNCTION_SAME:
			case OBJECT_SAME: {
				Label exit = new Label();
				javaWriter.iConst0();
				javaWriter.ifACmpNe(exit);
				javaWriter.iConst1();
				javaWriter.label(exit);
				break;
			}
			case ASSOC_NOTSAME:
			case GENERICMAP_NOTSAME:
			case ARRAY_NOTSAME:
			case FUNCTION_NOTSAME:
			case OBJECT_NOTSAME: {
				Label exit = new Label();
				javaWriter.iConst0();
				javaWriter.ifACmpEq(exit);
				javaWriter.iConst1();
				javaWriter.label(exit);
				break;
			}
			case GENERICMAP_GETOPTIONAL: {
				javaWriter.invokeVirtual(MAP_GET);
				break;
			}
			case GENERICMAP_PUT: {
				javaWriter.invokeVirtual(MAP_PUT);
				javaWriter.pop();
				break;
			}
			case GENERICMAP_CONTAINS:
				javaWriter.invokeVirtual(MAP_CONTAINS_KEY);
				break;
			case GENERICMAP_EQUALS:
				throw new UnsupportedOperationException("Not yet supported!");
			case GENERICMAP_NOTEQUALS:
				throw new UnsupportedOperationException("Not yet supported!");
			case ARRAY_INDEXGET: {
				ArrayTypeID type = (ArrayTypeID) expression.target.type;
				javaWriter.arrayLoad(context.getType(type.elementType));
				break;
			}
			case ARRAY_INDEXSET: {
				ArrayTypeID type = (ArrayTypeID) expression.target.type;
				javaWriter.arrayStore(context.getType(type.elementType));
				break;
			}
			case ARRAY_INDEXGETRANGE: {
				ArrayTypeID type = (ArrayTypeID) expression.target.type;

				expression.target.accept(this);
				Expression argument = expression.arguments.arguments[0];
				if (argument instanceof RangeExpression) {
					RangeExpression rangeArgument = (RangeExpression) argument;
					rangeArgument.from.accept(this);
					rangeArgument.to.accept(this);
				} else {
					argument.accept(this);
					javaWriter.dup();
					int tmp = javaWriter.local(Type.getType("zsynthetic/IntRange"));
					javaWriter.storeInt(tmp);
					javaWriter.getField("zsynthetic/IntRange", "from", "I");
					javaWriter.loadInt(tmp);
					javaWriter.getField("zsynthetic/IntRange", "to", "I");
				}

				if (type.elementType instanceof BasicTypeID) {
					switch ((BasicTypeID) type.elementType) {
						case BOOL:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_BOOLS);
							break;
						case BYTE:
						case SBYTE:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_BYTES);
							break;
						case SHORT:
						case USHORT:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_SHORTS);
							break;
						case INT:
						case UINT:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_INTS);
							break;
						case LONG:
						case ULONG:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_LONGS);
							break;
						case FLOAT:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_FLOATS);
							break;
						case DOUBLE:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_DOUBLES);
							break;
						case CHAR:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_CHARS);
							break;
						default:
							throw new IllegalArgumentException("Unknown basic type: " + type.elementType);
					}
				} else {
					javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_OBJECTS);
					javaWriter.checkCast(context.getInternalName(type));
				}
				break;
			}
			case ARRAY_CONTAINS:
				throw new UnsupportedOperationException("Not yet supported!");
			case ARRAY_EQUALS:
			case ARRAY_NOTEQUALS: {
				ArrayTypeID type = (ArrayTypeID) expression.target.type;
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

				if (builtin == BuiltinID.ARRAY_NOTEQUALS) {
					javaWriter.iConst1();
					javaWriter.iXor();
				}
				break;
			}
			case FUNCTION_CALL:
				javaWriter.invokeInterface(
						JavaMethod.getNativeVirtual(
								JavaClass.fromInternalName(context.getInternalName(expression.target.type), JavaClass.Kind.INTERFACE),
								"accept",
								context.getMethodSignature(expression.instancedHeader)));
				break;
			case AUTOOP_NOTEQUALS:
				throw new UnsupportedOperationException("Not yet supported!");
			default:
				throw new UnsupportedOperationException("Unknown builtin: " + builtin);
		}

		return null;
	}

	@Override
	public Void visitCallStatic(CallStaticExpression expression) {
		for (Expression argument : expression.arguments.arguments)
			argument.accept(this);

		BuiltinID builtin = expression.member.getBuiltin();
		if (builtin == null) {
			if (!checkAndExecuteMethodInfo(expression.member, expression.type))
				throw new IllegalStateException("Call target has no method info!");

			return null;
		}

		switch (builtin) {
			case BOOL_PARSE:
				javaWriter.invokeStatic(BOOLEAN_PARSE);
				break;
			case BYTE_PARSE:
				javaWriter.invokeStatic(INTEGER_PARSE_UNSIGNED);
				break;
			case BYTE_PARSE_WITH_BASE:
				javaWriter.invokeStatic(INTEGER_PARSE_UNSIGNED_WITH_BASE);
				break;
			case SBYTE_PARSE:
				javaWriter.invokeStatic(BYTE_PARSE);
				break;
			case SBYTE_PARSE_WITH_BASE:
				javaWriter.invokeStatic(BYTE_PARSE_WITH_BASE);
				break;
			case SHORT_PARSE:
				javaWriter.invokeStatic(SHORT_PARSE);
				break;
			case SHORT_PARSE_WITH_BASE:
				javaWriter.invokeStatic(SHORT_PARSE_WITH_BASE);
				break;
			case USHORT_PARSE:
				javaWriter.invokeStatic(INTEGER_PARSE_UNSIGNED);
				break;
			case USHORT_PARSE_WITH_BASE:
				javaWriter.invokeStatic(INTEGER_PARSE_UNSIGNED_WITH_BASE);
				break;
			case INT_PARSE:
				javaWriter.invokeStatic(INTEGER_PARSE);
				break;
			case INT_PARSE_WITH_BASE:
				javaWriter.invokeStatic(INTEGER_PARSE_WITH_BASE);
				break;
			case UINT_PARSE:
			case USIZE_PARSE:
				javaWriter.invokeStatic(INTEGER_PARSE_UNSIGNED);
				break;
			case UINT_PARSE_WITH_BASE:
			case USIZE_PARSE_WITH_BASE:
				javaWriter.invokeStatic(INTEGER_PARSE_UNSIGNED_WITH_BASE);
				break;
			case LONG_PARSE:
				javaWriter.invokeStatic(LONG_PARSE);
				break;
			case LONG_PARSE_WITH_BASE:
				javaWriter.invokeStatic(LONG_PARSE_WITH_BASE);
				break;
			case ULONG_PARSE:
				javaWriter.invokeStatic(LONG_PARSE_UNSIGNED);
				break;
			case ULONG_PARSE_WITH_BASE:
				javaWriter.invokeStatic(LONG_PARSE_UNSIGNED_WITH_BASE);
				break;
			case FLOAT_FROM_BITS:
				javaWriter.invokeStatic(FLOAT_FROM_BITS);
				break;
			case FLOAT_PARSE:
				javaWriter.invokeStatic(FLOAT_PARSE);
				break;
			case DOUBLE_FROM_BITS:
				javaWriter.invokeStatic(DOUBLE_FROM_BITS);
				break;
			case DOUBLE_PARSE:
				javaWriter.invokeStatic(DOUBLE_PARSE);
				break;
			default:
				throw new UnsupportedOperationException("Unknown builtin: " + builtin);
		}
		return null;
	}

	@Override
	public Void visitCapturedClosure(CapturedClosureExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	@Override
	public Void visitCapturedDirect(CapturedDirectExpression expression) {
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
	public Void visitCast(CastExpression expression) {
		expression.target.accept(this);

		BuiltinID builtin = expression.member.member.builtin;
		if (builtin == null) {
			if (!checkAndExecuteMethodInfo(expression.member, expression.type))
				throw new IllegalStateException("Call target has no method info!");

			return null;
		}

		switch (builtin) {
			case BOOL_TO_STRING:
				javaWriter.invokeStatic(BOOLEAN_TO_STRING);
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
				javaWriter.constant(0xFF);
				javaWriter.iAnd();
				javaWriter.invokeStatic(INTEGER_TO_STRING);
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
				javaWriter.invokeStatic(INTEGER_TO_STRING);
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
				javaWriter.invokeStatic(SHORT_TO_STRING);
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
				javaWriter.constant(0xFFFFL);
				javaWriter.iAnd();
				break;
			case USHORT_TO_FLOAT:
				javaWriter.constant(0xFFFFL);
				javaWriter.iAnd();
				javaWriter.i2f();
				break;
			case USHORT_TO_DOUBLE:
				javaWriter.constant(0xFFFFL);
				javaWriter.iAnd();
				javaWriter.i2d();
				break;
			case USHORT_TO_CHAR:
				javaWriter.constant(0xFFFFL);
				javaWriter.iAnd();
				break;
			case USHORT_TO_STRING:
				javaWriter.constant(0xFFFFL);
				javaWriter.iAnd();
				javaWriter.invokeStatic(INTEGER_TO_STRING);
				break;
			case INT_TO_BYTE:
				break;
			case INT_TO_SBYTE:
				javaWriter.i2b();
				break;
			case INT_TO_SHORT:
				javaWriter.i2s();
				break;
			case INT_TO_USHORT:
				break;
			case INT_TO_UINT:
			case INT_TO_USIZE:
				break;
			case INT_TO_LONG:
			case INT_TO_ULONG:
				javaWriter.i2l();
				break;
			case INT_TO_FLOAT:
				javaWriter.i2f();
				break;
			case INT_TO_DOUBLE:
				javaWriter.i2d();
				break;
			case INT_TO_CHAR:
				javaWriter.i2s();
				break;
			case INT_TO_STRING:
				javaWriter.invokeStatic(INTEGER_TO_STRING);
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
				javaWriter.invokeStatic(INTEGER_TO_UNSIGNED_STRING);
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
			case LONG_TO_STRING:
				javaWriter.invokeStatic(LONG_TO_STRING);
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
			case ULONG_TO_CHAR:
				javaWriter.l2i();
				javaWriter.i2s();
				break;
			case ULONG_TO_STRING:
				javaWriter.invokeStatic(LONG_TO_UNSIGNED_STRING);
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
			case FLOAT_TO_STRING:
				javaWriter.invokeStatic(FLOAT_TO_STRING);
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
			case DOUBLE_TO_STRING:
				javaWriter.invokeStatic(DOUBLE_TO_STRING);
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
				javaWriter.invokeStatic(CHARACTER_TO_STRING);
				break;
			case ENUM_TO_STRING:
				javaWriter.invokeVirtual(ENUM_NAME);
				break;
			default:
				throw new UnsupportedOperationException("Unknown builtin cast: " + builtin);
		}

		return null;
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
		javaWriter.label(end);
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
			if (!checkAndGetFieldInfo(expression.constant, true))
				throw new IllegalStateException("Call target has no field info!");

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
				DefinitionTypeID type = (DefinitionTypeID) expression.type;
				JavaClass cls = type.definition.getTag(JavaClass.class);
				javaWriter.invokeStatic(JavaMethod.getNativeStatic(cls, "values", "()[L" + cls.internalName + ";"));
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
		getJavaWriter().constant((int)expression.value);
		return null;
	}

    @Override
    public Void visitConstructorThisCall(ConstructorThisCallExpression expression) {
		javaWriter.loadObject(0);
		if (javaWriter.method.cls.isEnum()) {
			javaWriter.loadObject(1);
			javaWriter.loadInt(2);
		}

		for (Expression argument : expression.arguments.arguments) {
			argument.accept(this);
		}
		String internalName = context.getInternalName(expression.objectType);
        javaWriter.invokeSpecial(internalName, "<init>", javaWriter.method.cls.isEnum()
				? context.getEnumConstructorDescriptor(expression.constructor.getHeader())
				: context.getMethodDescriptor(expression.constructor.getHeader()));
        return null;
    }

    @Override
    public Void visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
        javaWriter.loadObject(0);
        for (Expression argument : expression.arguments.arguments) {
            argument.accept(this);
        }
        //No super calls in enums possible, and that's already handled in the enum constructor itself.
        javaWriter.invokeSpecial(
				context.getInternalName(expression.objectType),
				"<init>",
				context.getMethodDescriptor(expression.constructor.getHeader()));

        CompilerUtils.writeDefaultFieldInitializers(context, javaWriter, javaWriter.forDefinition, false);
        return null;
    }

    @Override
    public Void visitEnumConstant(EnumConstantExpression expression) {
        javaWriter.getStaticField(context.getInternalName(expression.type), expression.value.name, context.getDescriptor(expression.type));
        return null;
    }

	@Override
	public Void visitFunction(FunctionExpression expression) {
		CompilerUtils.tagMethodParameters(context, expression.header, false);

        if (expression.header.parameters.length == 0 && expression.body instanceof ReturnStatement && expression.body.hasTag(MatchExpression.class) && expression.closure.captures.isEmpty()) {
            ((ReturnStatement) expression.body).value.accept(this);
            return null;
        }
        final String signature = context.getMethodSignature(expression.header);
		final String name = context.getLambdaCounter();

		final JavaMethod methodInfo = JavaMethod.getNativeVirtual(javaWriter.method.cls, "accept", signature);
		final ClassWriter lambdaCW = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
		lambdaCW.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, name, null, "java/lang/Object", new String[]{context.getInternalName(new FunctionTypeID(null, expression.header, null))});
		final JavaWriter functionWriter = new JavaWriter(lambdaCW, methodInfo, null, signature, null, "java/lang/Override");

		javaWriter.newObject(name);
		javaWriter.dup();

		final String constructorDesc = calcFunctionSignature(expression.closure);


		final JavaWriter constructorWriter = new JavaWriter(lambdaCW, JavaMethod.getConstructor(javaWriter.method.cls, constructorDesc, Opcodes.ACC_PUBLIC), null, null, null);
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
			constructorWriter.putField(name, "captured" + i, type.getDescriptor());
		}

		constructorWriter.pop();

		javaWriter.invokeSpecial(name, "<init>", constructorDesc);

		constructorWriter.ret();
		constructorWriter.end();


        functionWriter.start();


		final JavaStatementVisitor CSV = new JavaStatementVisitor(context, new JavaExpressionVisitor(context, functionWriter) {
			@Override
			public Void visitGetLocalVariable(GetLocalVariableExpression varExpression) {
				final int position = calculateMemberPosition(varExpression, expression);
				functionWriter.loadObject(0);
				functionWriter.getField(name, "captured" + position, context.getDescriptor(varExpression.variable.type));
				return null;
			}

			@Override
			public Void visitCapturedParameter(CapturedParameterExpression varExpression) {
				final int position = calculateMemberPosition(varExpression, expression);
				functionWriter.loadObject(0);
				functionWriter.getField(name, "captured" + position, context.getDescriptor(varExpression.parameter.type));
				return null;
			}
		});

		expression.body.accept(CSV);

        functionWriter.ret();
		functionWriter.end();
		lambdaCW.visitEnd();

		context.register(name, lambdaCW.toByteArray());

		try (FileOutputStream out = new FileOutputStream(name + ".class")) {
			out.write(lambdaCW.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
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
		throw new CompileException(localVariableExpression.position, CompileExceptionCode.INTERNAL_ERROR, "Captured Statement error");
	}

	private static int calculateMemberPosition(CapturedParameterExpression functionParameterExpression, FunctionExpression expression) {
		int h = 1;//expression.header.parameters.length;
    	for (CapturedExpression capture : expression.closure.captures) {
			if (capture instanceof CapturedParameterExpression && ((CapturedParameterExpression) capture).parameter == functionParameterExpression.parameter)
				return h;
			h++;
		}
		throw new CompileException(functionParameterExpression.position, CompileExceptionCode.INTERNAL_ERROR, "Captured Statement error");
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
		expression.accept(this);
		if (!checkAndGetFieldInfo(expression.field, false))
			throw new IllegalStateException("Missing field info on a field member!");
		return null;
	}

	@Override
	public Void visitGetFunctionParameter(GetFunctionParameterExpression expression) {
		JavaParameterInfo parameter = expression.parameter.getTag(JavaParameterInfo.class);

		if (parameter == null) {
			throw new CompileException(expression.position, CompileExceptionCode.LAMBDA_HEADER_INVALID, "Could not resolve lambda parameter" + expression.parameter);
		}

        javaWriter.load(context.getType(expression.parameter.type), parameter.index);
        return null;
    }

	@Override
	public Void visitGetLocalVariable(GetLocalVariableExpression expression) {
		final Label label = new Label();
		final JavaLocalVariableInfo tag = expression.variable.getTag(JavaLocalVariableInfo.class);
		tag.end = label;
		javaWriter.load(tag.type, tag.local);
		javaWriter.label(label);
		return null;
	}

	@Override
	public Void visitGetMatchingVariantField(GetMatchingVariantField expression) {
		javaWriter.loadObject(0);
		final ITypeID type = expression.value.option.getParameterType(expression.index);
		final JavaVariantOption tag = expression.value.option.getTag(JavaVariantOption.class);
		javaWriter.checkCast(tag.variantOptionClass.internalName);
		javaWriter.getField(new JavaField(tag.variantOptionClass, "field" + expression.index, context.getDescriptor(type)));
		return null;
	}

	@Override
	public Void visitGetStaticField(GetStaticFieldExpression expression) {
		if (!checkAndGetFieldInfo(expression.field, true))
			throw new IllegalStateException("Missing field info on a field member!");
		return null;
	}

	@Override
	public Void visitGetter(GetterExpression expression) {
		expression.target.accept(this);

		BuiltinID builtin = expression.getter.member.builtin;
		if (builtin == null) {
			if (!checkAndExecuteMethodInfo(expression.getter, expression.type))
				throw new IllegalStateException("Call target has no method info!");

			return null;
		}

		switch (builtin) {
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
				AssocTypeID type = (AssocTypeID) expression.target.type;
				ArrayTypeID result = new ArrayTypeID(null, type.keyType, 1, UniqueStorageTag.INSTANCE);
				Type resultType = context.getType(result);

				javaWriter.invokeVirtual(MAP_KEYS);
				javaWriter.dup();
				javaWriter.invokeVirtual(COLLECTION_SIZE);
				javaWriter.newArray(resultType);
				javaWriter.invokeVirtual(COLLECTION_TOARRAY);
				javaWriter.checkCast(resultType);
				break;
			}
			case ASSOC_VALUES: {
				AssocTypeID type = (AssocTypeID) expression.target.type;
				ArrayTypeID result = new ArrayTypeID(null, type.valueType, 1, UniqueStorageTag.INSTANCE);
				Type resultType = context.getType(result);

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
				RangeTypeID type = (RangeTypeID)expression.target.type;
				JavaClass cls = context.getRange(type).cls;
				javaWriter.getField(cls.internalName, "from", context.getDescriptor(type.baseType));
				break;
			}
			case RANGE_TO:
				RangeTypeID type = (RangeTypeID)expression.target.type;
				JavaClass cls = context.getRange(type).cls;
				javaWriter.getField(cls.internalName, "to", context.getDescriptor(type.baseType));
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
        for (int i = 0; i < expression.keys.length; i++) {
            javaWriter.dup();
            expression.keys[i].accept(this);
            expression.values[i].accept(this);
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
		if (expression.value.type instanceof StringTypeID)
			javaWriter.invokeVirtual(OBJECT_HASHCODE);

		//TODO replace with beforeSwitch visitor or similar
		for (MatchExpression.Case aCase : expression.cases) {
			if (aCase.key instanceof VariantOptionSwitchValue) {
				VariantOptionSwitchValue variantOptionSwitchValue = (VariantOptionSwitchValue) aCase.key;
				JavaVariantOption option = variantOptionSwitchValue.option.getTag(JavaVariantOption.class);
				javaWriter.invokeVirtual(JavaMethod.getNativeVirtual(option.variantClass, "getDenominator", "()I"));
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
		return null;
	}

	private static boolean hasNoDefault(MatchExpression switchStatement) {
		for (MatchExpression.Case switchCase : switchStatement.cases)
			if (switchCase.key == null) return false;
		return true;
	}

    @Override
    public Void visitNew(NewExpression expression) {
		// TODO: this code is incorrect!
		JavaMethod method = expression.constructor.getTag(JavaMethod.class);

        final String type;
        if (expression.type instanceof DefinitionTypeID)
            type = ((DefinitionTypeID) expression.type).definition.name;
        else
            type = context.getDescriptor(expression.type);

        javaWriter.newObject(type);
        javaWriter.dup();

		for (Expression argument : expression.arguments.arguments) {
			argument.accept(this);
		}

        javaWriter.invokeSpecial(method);
		return null;
	}

	@Override
	public Void visitNull(NullExpression expression) {
		if (expression.type.withoutOptional() == BasicTypeID.USIZE)
			javaWriter.constant(-1); // special case: usize? null = -1
		else
			javaWriter.aConstNull();
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
		javaWriter.invokeSpecial(AssertionError.class, "<init>", "(Ljava/lang/String;)V");
		javaWriter.aThrow();
		return null;
	}

	@Override
	public Void visitPostCall(PostCallExpression expression) {
		expression.target.accept(this);
		javaWriter.dup(context.getType(expression.type));
		if (!checkAndExecuteMethodInfo(expression.member, expression.type))
			throw new IllegalStateException("Call target has no method info!");

		return null;
	}

    @Override
    public Void visitRange(RangeExpression expression) {
		RangeTypeID type = (RangeTypeID)expression.type;
		JavaSynthesizedClass cls = context.getRange(type);
		javaWriter.newObject(cls.cls.internalName);
		javaWriter.dup();
		expression.from.accept(this);
		expression.to.accept(this);
		javaWriter.invokeSpecial(cls.cls.internalName, "<init>", "(" + context.getDescriptor(type.baseType) + context.getDescriptor(type.baseType) + ")V");

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
		expression.target.accept(this);
		expression.value.accept(this);
		if (!checkAndPutFieldInfo(expression.field, false))
			throw new IllegalStateException("Missing field info on a field member!");
		return null;
	}

    @Override
    public Void visitSetFunctionParameter(SetFunctionParameterExpression expression) {
        expression.value.accept(this);
        JavaParameterInfo parameter = expression.parameter.getTag(JavaParameterInfo.class);
        javaWriter.store(context.getType(expression.type), parameter.index);
        return null;
    }

	@Override
	public Void visitSetLocalVariable(SetLocalVariableExpression expression) {
		expression.value.accept(this);
		Label label = new Label();
		javaWriter.label(label);
		final JavaLocalVariableInfo tag = expression.variable.getTag(JavaLocalVariableInfo.class);
		tag.end = label;

		javaWriter.store(tag.type, tag.local);

		return null;
	}

	@Override
	public Void visitSetStaticField(SetStaticFieldExpression expression) {
		if (expression.field.isFinal())
			throw new CompileException(expression.position, CompileExceptionCode.CANNOT_SET_FINAL_VARIABLE, "Cannot set a final field!");

		expression.value.accept(this);
		if (!checkAndPutFieldInfo(expression.field, true))
			throw new IllegalStateException("Missing field info on a field member!");
		return null;
	}

	@Override
	public Void visitSetter(SetterExpression expression) {
		return null;
	}

	@Override
	public Void visitStaticGetter(StaticGetterExpression expression) {
		BuiltinID builtin = expression.getter.member.builtin;
		if (builtin == null) {
			if (!checkAndExecuteMethodInfo(expression.getter, expression.type))
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
				DefinitionTypeID type = (DefinitionTypeID) expression.type;
				JavaClass cls = type.definition.getTag(JavaClass.class);
				javaWriter.invokeStatic(JavaMethod.getNativeStatic(cls, "values", "()[L" + cls.internalName + ";"));
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
	
	@Override
	public Void visitStorageCast(StorageCastExpression expression) {
		expression.value.accept(this);
		
		if (expression.type.isDestructible()) { // only destructible types matter here; nondestructible types never need conversion
			StorageTag fromTag = expression.value.type.getStorage();
			StorageTag toTag = expression.type.getStorage();
			if (fromTag == SharedStorageTag.INSTANCE && toTag == BorrowStorageTag.INVOCATION) {
				// Shared<T>.get()
				javaWriter.invokeVirtual(SHARED_GET);
			} else if (fromTag == UniqueStorageTag.INSTANCE && toTag == SharedStorageTag.INSTANCE) {
				// new Shared<T>(value)
				javaWriter.newObject("zsynthetic/Shared");
				javaWriter.dup();
				javaWriter.invokeSpecial(SHARED_INIT);
			}
		}
		
		return null;
	}

	@Override
	public Void visitSupertypeCast(SupertypeCastExpression expression) {
		return null; // nothing to do
	}

	@Override
	public Void visitThis(ThisExpression expression) {
		javaWriter.loadObject(0);
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
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Void visitTryRethrowAsResult(TryRethrowAsResultExpression expression) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Void visitVariantValue(VariantValueExpression expression) {
		JavaVariantOption tag = expression.option.getTag(JavaVariantOption.class);
		final String internalName = tag.variantOptionClass.internalName;
		javaWriter.newObject(internalName);
		javaWriter.dup();

		for (Expression argument : expression.arguments) {
			argument.accept(this);
		}

		final StringBuilder builder = new StringBuilder("(");
		for (ITypeID type : expression.option.getOption().types) {
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
		expression.value.type.accept(new JavaBoxingTypeVisitor(javaWriter));
		return null;
	}

    public JavaWriter getJavaWriter() {
        return javaWriter;
    }

    //Will return true if a JavaMethodInfo.class tag exists, and will compile that tag
    private boolean checkAndExecuteMethodInfo(DefinitionMemberRef member, ITypeID resultType) {
        JavaMethod methodInfo = member.getTag(JavaMethod.class);
        if (methodInfo == null)
            return false;

        if (methodInfo.kind == JavaMethod.Kind.STATIC) {
            getJavaWriter().invokeStatic(methodInfo);
        } else {
            getJavaWriter().invokeVirtual(methodInfo);
        }
		if (methodInfo.genericResult)
			getJavaWriter().checkCast(context.getInternalName(resultType));

        return true;
    }

    //Will return true if a JavaFieldInfo.class tag exists, and will compile that tag
    public boolean checkAndPutFieldInfo(FieldMemberRef field, boolean isStatic) {
		JavaField fieldInfo = field.getTag(JavaField.class);
        if (fieldInfo == null)
            return false;
        //TODO Remove isStatic
        if (field.isStatic() || isStatic) {
            getJavaWriter().putStaticField(fieldInfo);
        } else {
            getJavaWriter().putField(fieldInfo);
        }
        return true;
    }

	public boolean checkAndGetFieldInfo(ConstMemberRef field, boolean isStatic) {
		final JavaField fieldInfo = field.getTag(JavaField.class);
		if (fieldInfo == null)
			return false;

		getJavaWriter().getStaticField(fieldInfo);
		return true;
	}

	public boolean checkAndGetFieldInfo(FieldMemberRef field, boolean isStatic) {
		final JavaField fieldInfo = field.getTag(JavaField.class);
		if (fieldInfo == null)
			return false;

		//TODO Remove isStatic
		if (field.isStatic() || isStatic) {
			getJavaWriter().getStaticField(fieldInfo);
		} else {
			getJavaWriter().getField(fieldInfo);
		}
		return true;
	}
}
