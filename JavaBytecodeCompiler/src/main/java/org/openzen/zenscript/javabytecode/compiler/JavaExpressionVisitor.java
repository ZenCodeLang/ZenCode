package org.openzen.zenscript.javabytecode.compiler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.StringJoiner;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.member.ref.ConstMemberRef;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.FieldMemberRef;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.implementations.IntRange;
import org.openzen.zenscript.javabytecode.*;

import java.io.FileOutputStream;
import java.io.IOException;

public class JavaExpressionVisitor implements ExpressionVisitor<Void> {
	private static final int PUBLIC = Opcodes.ACC_PUBLIC;
	private static final int STATIC = Opcodes.ACC_STATIC;
	private static final int PUBLIC_STATIC = PUBLIC | STATIC;

	private static final JavaClassInfo BOOLEAN = JavaClassInfo.get(Boolean.class);
	private static final JavaMethodInfo BOOLEAN_PARSE = new JavaMethodInfo(BOOLEAN, "parseBoolean", "(Ljava/lang/String;)Z", PUBLIC_STATIC);
	private static final JavaMethodInfo BOOLEAN_TO_STRING = new JavaMethodInfo(BOOLEAN, "toString", "(Z)Ljava/lang/String;", PUBLIC_STATIC);
	private static final JavaClassInfo BYTE = JavaClassInfo.get(Byte.class);
	private static final JavaMethodInfo BYTE_PARSE = new JavaMethodInfo(BYTE, "parseByte", "(Ljava/lang/String;)B", PUBLIC_STATIC);
	private static final JavaMethodInfo BYTE_PARSE_WITH_BASE = new JavaMethodInfo(BYTE, "parseByte", "(Ljava/lang/String;I)B", PUBLIC_STATIC);
	private static final JavaFieldInfo BYTE_MIN_VALUE = new JavaFieldInfo(BYTE, "MIN_VALUE", "B");
	private static final JavaFieldInfo BYTE_MAX_VALUE = new JavaFieldInfo(BYTE, "MAX_VALUE", "B");
	private static final JavaMethodInfo BYTE_TO_STRING = new JavaMethodInfo(BYTE, "toString", "(B)Ljava/lang/String;", PUBLIC_STATIC);
	private static final JavaClassInfo SHORT = JavaClassInfo.get(Short.class);
	private static final JavaMethodInfo SHORT_PARSE = new JavaMethodInfo(SHORT, "parseShort", "(Ljava/lang/String;)S", PUBLIC_STATIC);
	private static final JavaMethodInfo SHORT_PARSE_WITH_BASE = new JavaMethodInfo(SHORT, "parseShort", "(Ljava/lang/String;I)S", PUBLIC_STATIC);
	private static final JavaFieldInfo SHORT_MIN_VALUE = new JavaFieldInfo(SHORT, "MIN_VALUE", "S");
	private static final JavaFieldInfo SHORT_MAX_VALUE = new JavaFieldInfo(SHORT, "MAX_VALUE", "S");
	private static final JavaMethodInfo SHORT_TO_STRING = new JavaMethodInfo(SHORT, "toString", "(S)Ljava/lang/String;", PUBLIC_STATIC);
	private static final JavaClassInfo INTEGER = JavaClassInfo.get(Integer.class);
	private static final JavaMethodInfo INTEGER_COMPARE_UNSIGNED = new JavaMethodInfo(INTEGER, "compareUnsigned", "(II)I", PUBLIC_STATIC);
	private static final JavaMethodInfo INTEGER_DIVIDE_UNSIGNED = new JavaMethodInfo(INTEGER, "divideUnsigned", "(II)I", PUBLIC_STATIC);
	private static final JavaMethodInfo INTEGER_REMAINDER_UNSIGNED = new JavaMethodInfo(INTEGER, "remainderUnsigned", "(II)I", PUBLIC_STATIC);
	private static final JavaMethodInfo INTEGER_NUMBER_OF_TRAILING_ZEROS = new JavaMethodInfo(INTEGER, "numberOfTrailingZeros", "(I)I", PUBLIC_STATIC);
	private static final JavaMethodInfo INTEGER_NUMBER_OF_LEADING_ZEROS = new JavaMethodInfo(INTEGER, "numberOfLeadingZeros", "(I)I", PUBLIC_STATIC);
	private static final JavaMethodInfo INTEGER_PARSE = new JavaMethodInfo(INTEGER, "parseInt", "(Ljava/lang/String;)I", PUBLIC_STATIC);
	private static final JavaMethodInfo INTEGER_PARSE_WITH_BASE = new JavaMethodInfo(INTEGER, "parseInt", "(Ljava/lang/String;I)I", PUBLIC_STATIC);
	private static final JavaMethodInfo INTEGER_PARSE_UNSIGNED = new JavaMethodInfo(INTEGER, "parseUnsignedInt", "(Ljava/lang/String;)I", PUBLIC_STATIC);
	private static final JavaMethodInfo INTEGER_PARSE_UNSIGNED_WITH_BASE = new JavaMethodInfo(INTEGER, "parseUnsignedInt", "(Ljava/lang/String;I)I", PUBLIC_STATIC);
	private static final JavaMethodInfo INTEGER_HIGHEST_ONE_BIT = new JavaMethodInfo(INTEGER, "highestOneBit", "(I)I", PUBLIC_STATIC);
	private static final JavaMethodInfo INTEGER_LOWEST_ONE_BIT = new JavaMethodInfo(INTEGER, "lowestOneBit", "(I)I", PUBLIC_STATIC);
	private static final JavaMethodInfo INTEGER_BIT_COUNT = new JavaMethodInfo(INTEGER, "bitCount", "(I)I", PUBLIC_STATIC);
	private static final JavaFieldInfo INTEGER_MIN_VALUE = new JavaFieldInfo(INTEGER, "MIN_VALUE", "I");
	private static final JavaFieldInfo INTEGER_MAX_VALUE = new JavaFieldInfo(INTEGER, "MAX_VALUE", "I");
	private static final JavaMethodInfo INTEGER_TO_STRING = new JavaMethodInfo(INTEGER, "toString", "(I)Ljava/lang/String;", PUBLIC_STATIC);
	private static final JavaMethodInfo INTEGER_TO_UNSIGNED_STRING = new JavaMethodInfo(INTEGER, "toUnsignedString", "(I)Ljava/lang/String;", PUBLIC_STATIC);
	private static final JavaClassInfo LONG = new JavaClassInfo(Type.getInternalName(Long.class));
	private static final JavaMethodInfo LONG_COMPARE = new JavaMethodInfo(LONG, "compare", "(JJ)I", PUBLIC_STATIC);
	private static final JavaMethodInfo LONG_COMPARE_UNSIGNED = new JavaMethodInfo(LONG, "compareUnsigned", "(JJ)I", PUBLIC_STATIC);
	private static final JavaMethodInfo LONG_DIVIDE_UNSIGNED = new JavaMethodInfo(LONG, "divideUnsigned", "(JJ)J", PUBLIC_STATIC);
	private static final JavaMethodInfo LONG_REMAINDER_UNSIGNED = new JavaMethodInfo(LONG, "remainderUnsigned", "(JJ)J", PUBLIC_STATIC);
	private static final JavaMethodInfo LONG_NUMBER_OF_TRAILING_ZEROS = new JavaMethodInfo(LONG, "numberOfTrailingZeros", "(J)I", PUBLIC_STATIC);
	private static final JavaMethodInfo LONG_NUMBER_OF_LEADING_ZEROS = new JavaMethodInfo(LONG, "numberOfLeadingZeros", "(J)I", PUBLIC_STATIC);
	private static final JavaMethodInfo LONG_PARSE = new JavaMethodInfo(LONG, "parseLong", "(Ljava/lang/String;)J", PUBLIC_STATIC);
	private static final JavaMethodInfo LONG_PARSE_WITH_BASE = new JavaMethodInfo(LONG, "parseLong", "(Ljava/lang/String;I)J", PUBLIC_STATIC);
	private static final JavaMethodInfo LONG_PARSE_UNSIGNED = new JavaMethodInfo(LONG, "parseUnsignedLong", "(Ljava/lang/String;)J", PUBLIC_STATIC);
	private static final JavaMethodInfo LONG_PARSE_UNSIGNED_WITH_BASE = new JavaMethodInfo(LONG, "parseUnsignedLong", "(Ljava/lang/String;I)J", PUBLIC_STATIC);
	private static final JavaMethodInfo LONG_HIGHEST_ONE_BIT = new JavaMethodInfo(LONG, "highestOneBit", "(J)J", PUBLIC_STATIC);
	private static final JavaMethodInfo LONG_LOWEST_ONE_BIT = new JavaMethodInfo(LONG, "lowestOneBit", "(J)J", PUBLIC_STATIC);
	private static final JavaMethodInfo LONG_BIT_COUNT = new JavaMethodInfo(LONG, "bitCount", "(J)I", PUBLIC_STATIC);
	private static final JavaFieldInfo LONG_MIN_VALUE = new JavaFieldInfo(LONG, "MIN_VALUE", "J");
	private static final JavaFieldInfo LONG_MAX_VALUE = new JavaFieldInfo(LONG, "MAX_VALUE", "J");
	private static final JavaMethodInfo LONG_TO_STRING = new JavaMethodInfo(LONG, "toString", "(J)Ljava/lang/String;", PUBLIC_STATIC);
	private static final JavaMethodInfo LONG_TO_UNSIGNED_STRING = new JavaMethodInfo(LONG, "toUnsignedString", "(J)Ljava/lang/String;", PUBLIC_STATIC);
	private static final JavaClassInfo FLOAT = new JavaClassInfo(Type.getInternalName(Float.class));
	private static final JavaMethodInfo FLOAT_COMPARE = new JavaMethodInfo(FLOAT, "compare", "(FF)I", PUBLIC_STATIC);
	private static final JavaMethodInfo FLOAT_PARSE = new JavaMethodInfo(FLOAT, "parseFloat", "(Ljava/lang/String;)F", PUBLIC_STATIC);
	private static final JavaMethodInfo FLOAT_FROM_BITS = new JavaMethodInfo(FLOAT, "intBitsToFloat", "(I)F", PUBLIC_STATIC);
	private static final JavaMethodInfo FLOAT_BITS = new JavaMethodInfo(FLOAT, "floatToRawIntBits", "(F)I", PUBLIC_STATIC);
	private static final JavaFieldInfo FLOAT_MIN_VALUE = new JavaFieldInfo(FLOAT, "MIN_VALUE", "F");
	private static final JavaFieldInfo FLOAT_MAX_VALUE = new JavaFieldInfo(FLOAT, "MAX_VALUE", "F");
	private static final JavaMethodInfo FLOAT_TO_STRING = new JavaMethodInfo(FLOAT, "toString", "(F)Ljava/lang/String;", PUBLIC_STATIC);
	private static final JavaClassInfo DOUBLE = new JavaClassInfo(Type.getInternalName(Double.class));
	private static final JavaMethodInfo DOUBLE_COMPARE = new JavaMethodInfo(DOUBLE, "compare", "(DD)I", PUBLIC_STATIC);
	private static final JavaMethodInfo DOUBLE_PARSE = new JavaMethodInfo(DOUBLE, "parseDouble", "(Ljava/lang/String;)D", PUBLIC_STATIC);
	private static final JavaMethodInfo DOUBLE_FROM_BITS = new JavaMethodInfo(DOUBLE, "longBitsToDouble", "(J)D", PUBLIC_STATIC);
	private static final JavaMethodInfo DOUBLE_BITS = new JavaMethodInfo(DOUBLE, "doubleToRawLongBits", "(D)J", PUBLIC_STATIC);
	private static final JavaFieldInfo DOUBLE_MIN_VALUE = new JavaFieldInfo(DOUBLE, "MIN_VALUE", "D");
	private static final JavaFieldInfo DOUBLE_MAX_VALUE = new JavaFieldInfo(DOUBLE, "MAX_VALUE", "D");
	private static final JavaMethodInfo DOUBLE_TO_STRING = new JavaMethodInfo(DOUBLE, "toString", "(D)Ljava/lang/String;", PUBLIC_STATIC);
	private static final JavaClassInfo CHARACTER = new JavaClassInfo(Type.getInternalName(Character.class));
	private static final JavaMethodInfo CHARACTER_TO_LOWER_CASE = new JavaMethodInfo(CHARACTER, "toLowerCase", "()C", PUBLIC);
	private static final JavaMethodInfo CHARACTER_TO_UPPER_CASE = new JavaMethodInfo(CHARACTER, "toUpperCase", "()C", PUBLIC);
	private static final JavaFieldInfo CHARACTER_MIN_VALUE = new JavaFieldInfo(CHARACTER, "MIN_VALUE", "C");
	private static final JavaFieldInfo CHARACTER_MAX_VALUE = new JavaFieldInfo(CHARACTER, "MAX_VALUE", "C");
	private static final JavaMethodInfo CHARACTER_TO_STRING = new JavaMethodInfo(CHARACTER, "toString", "(C)Ljava/lang/String;", PUBLIC_STATIC);
	private static final JavaClassInfo STRING = new JavaClassInfo(Type.getInternalName(String.class));
	private static final JavaMethodInfo STRING_COMPARETO = new JavaMethodInfo(STRING, "compareTo", "(Ljava/lang/String;)I", PUBLIC);
	private static final JavaMethodInfo STRING_CONCAT = new JavaMethodInfo(STRING, "concat", "(Ljava/lang/String;)Ljava/lang/String;", PUBLIC);
	private static final JavaMethodInfo STRING_CHAR_AT = new JavaMethodInfo(STRING, "charAt", "(I)C", PUBLIC);
	private static final JavaMethodInfo STRING_SUBSTRING = new JavaMethodInfo(STRING, "substring", "(II)Ljava/lang/String;", PUBLIC);
	private static final JavaMethodInfo STRING_TRIM = new JavaMethodInfo(STRING, "trim", "()Ljava/lang/String;", PUBLIC);
	private static final JavaMethodInfo STRING_TO_LOWER_CASE = new JavaMethodInfo(STRING, "toLowerCase", "()Ljava/lang/String;", PUBLIC);
	private static final JavaMethodInfo STRING_TO_UPPER_CASE = new JavaMethodInfo(STRING, "toUpperCase", "()Ljava/lang/String;", PUBLIC);
	private static final JavaMethodInfo STRING_LENGTH = new JavaMethodInfo(STRING, "length", "()I", PUBLIC);
	private static final JavaMethodInfo STRING_CHARACTERS = new JavaMethodInfo(STRING, "toCharArray", "()[C", PUBLIC);
	private static final JavaMethodInfo STRING_ISEMPTY = new JavaMethodInfo(STRING, "isEmpty", "()Z", PUBLIC);
	private static final JavaClassInfo ENUM = new JavaClassInfo(Type.getInternalName(Enum.class));
	private static final JavaMethodInfo ENUM_COMPARETO = new JavaMethodInfo(STRING, "compareTo", "(Ljava/lang/Enum;)I", PUBLIC);
	private static final JavaMethodInfo ENUM_NAME = new JavaMethodInfo(STRING, "name", "()Ljava/lang/String;", PUBLIC);
	private static final JavaMethodInfo ENUM_ORDINAL = new JavaMethodInfo(STRING, "ordinal", "()I", PUBLIC);
	private static final JavaClassInfo MAP = new JavaClassInfo(Type.getInternalName(Map.class));
	private static final JavaMethodInfo MAP_GET = new JavaMethodInfo(MAP, "get", "(Ljava/lang/Object;)Ljava/lang/Object;", PUBLIC);
	private static final JavaMethodInfo MAP_PUT = new JavaMethodInfo(MAP, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", PUBLIC);
	private static final JavaMethodInfo MAP_CONTAINS_KEY = new JavaMethodInfo(MAP, "containsKey", "(Ljava/lang/Object;)Z", PUBLIC);
	private static final JavaMethodInfo MAP_SIZE = new JavaMethodInfo(MAP, "size", "()I", PUBLIC);
	private static final JavaMethodInfo MAP_ISEMPTY = new JavaMethodInfo(MAP, "isEmpty", "()Z", PUBLIC);
	private static final JavaMethodInfo MAP_KEYS = new JavaMethodInfo(MAP, "keys", "()Ljava/lang/Object;", PUBLIC);
	private static final JavaMethodInfo MAP_VALUES = new JavaMethodInfo(MAP, "values", "()Ljava/lang/Object;", PUBLIC);
	private static final JavaClassInfo ARRAYS = new JavaClassInfo(Type.getInternalName(Arrays.class));
	private static final JavaMethodInfo ARRAYS_COPY_OF_RANGE_OBJECTS = new JavaMethodInfo(ARRAYS, "copyOfRange", "([Ljava/lang/Object;II)[Ljava/lang/Object;", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_COPY_OF_RANGE_BOOLS = new JavaMethodInfo(ARRAYS, "copyOfRange", "([ZII)[Z", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_COPY_OF_RANGE_BYTES = new JavaMethodInfo(ARRAYS, "copyOfRange", "([BII)[B", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_COPY_OF_RANGE_SHORTS = new JavaMethodInfo(ARRAYS, "copyOfRange", "([SII)[S", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_COPY_OF_RANGE_INTS = new JavaMethodInfo(ARRAYS, "copyOfRange", "([III)[I", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_COPY_OF_RANGE_LONGS = new JavaMethodInfo(ARRAYS, "copyOfRange", "([JII)[J", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_COPY_OF_RANGE_FLOATS = new JavaMethodInfo(ARRAYS, "copyOfRange", "([FII)[F", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_COPY_OF_RANGE_DOUBLES = new JavaMethodInfo(ARRAYS, "copyOfRange", "([DII)[D", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_COPY_OF_RANGE_CHARS = new JavaMethodInfo(ARRAYS, "copyOfRange", "([CII)[C", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_EQUALS_OBJECTS = new JavaMethodInfo(ARRAYS, "equals", "([Ljava/lang/Object[Ljava/lang/Object)Z", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_EQUALS_BOOLS = new JavaMethodInfo(ARRAYS, "equals", "([Z[Z)Z", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_EQUALS_BYTES = new JavaMethodInfo(ARRAYS, "equals", "([B[B)Z", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_EQUALS_SHORTS = new JavaMethodInfo(ARRAYS, "equals", "([S[S)Z", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_EQUALS_INTS = new JavaMethodInfo(ARRAYS, "equals", "([I[I)Z", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_EQUALS_LONGS = new JavaMethodInfo(ARRAYS, "equals", "([L[L)Z", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_EQUALS_FLOATS = new JavaMethodInfo(ARRAYS, "equals", "([F[F)Z", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_EQUALS_DOUBLES = new JavaMethodInfo(ARRAYS, "equals", "([D[D)Z", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_EQUALS_CHARS = new JavaMethodInfo(ARRAYS, "equals", "([C[C)Z", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_DEEPHASHCODE = new JavaMethodInfo(ARRAYS, "deepHashCode", "([Ljava/lang/Object;)", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_HASHCODE_BOOLS = new JavaMethodInfo(ARRAYS, "hashCode", "([Z)I", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_HASHCODE_BYTES = new JavaMethodInfo(ARRAYS, "hashCode", "([B)I", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_HASHCODE_SHORTS = new JavaMethodInfo(ARRAYS, "hashCode", "([S)I", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_HASHCODE_INTS = new JavaMethodInfo(ARRAYS, "hashCode", "([I)I", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_HASHCODE_LONGS = new JavaMethodInfo(ARRAYS, "hashCode", "([L)I", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_HASHCODE_FLOATS = new JavaMethodInfo(ARRAYS, "hashCode", "([F)I", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_HASHCODE_DOUBLES = new JavaMethodInfo(ARRAYS, "hashCode", "([D)I", PUBLIC_STATIC);
	private static final JavaMethodInfo ARRAYS_HASHCODE_CHARS = new JavaMethodInfo(ARRAYS, "hashCode", "([C)I", PUBLIC_STATIC);
	private static final JavaClassInfo OBJECT = JavaClassInfo.get(Object.class);
	private static final JavaMethodInfo OBJECT_HASHCODE = new JavaMethodInfo(OBJECT, "hashCode", "()I", PUBLIC);
	private static final JavaClassInfo COLLECTION = JavaClassInfo.get(Collection.class);
	private static final JavaMethodInfo COLLECTION_SIZE = new JavaMethodInfo(COLLECTION, "size", "()I", PUBLIC);
	private static final JavaMethodInfo COLLECTION_TOARRAY = new JavaMethodInfo(COLLECTION, "toArray", "([Ljava/lang/Object;)[Ljava/lang/Object;", PUBLIC);

    private final JavaWriter javaWriter;
    private final JavaCapturedExpressionVisitor capturedExpressionVisitor = new JavaCapturedExpressionVisitor(this);

    public JavaExpressionVisitor(JavaWriter javaWriter) {
        this.javaWriter = javaWriter;
    }

    private static Class<?> getForEquals(ITypeID id) {
        if (CompilerUtils.isPrimitive(id))
            return id.accept(JavaTypeClassVisitor.INSTANCE);
        return Object.class;
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
        Type type = Type.getType(expression.type.accept(JavaTypeClassVisitor.INSTANCE).getComponentType());
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
					expression.left.accept(this);
					expression.right.accept(this);
					compareInt(expression.comparison);
					break;
				case UINT_COMPARE:
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
			if (!checkAndExecuteMethodInfo(expression.operator))
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
			case EQ: javaWriter.ifICmpEQ(isTrue); break;
			case NE: javaWriter.ifICmpNE(isTrue); break;
			case GT: javaWriter.ifICmpGT(isTrue); break;
			case GE: javaWriter.ifICmpGE(isTrue); break;
			case LT: javaWriter.ifICmpLT(isTrue); break;
			case LE: javaWriter.ifICmpLE(isTrue); break;
			default: throw new IllegalStateException("Invalid comparator: " + comparator);
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
			case EQ: javaWriter.ifEQ(isTrue); break;
			case NE: javaWriter.ifNE(isTrue); break;
			case GT: javaWriter.ifGT(isTrue); break;
			case GE: javaWriter.ifGE(isTrue); break;
			case LT: javaWriter.ifLT(isTrue); break;
			case LE: javaWriter.ifLE(isTrue); break;
			default: throw new IllegalStateException("Invalid comparator: " + comparator);
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

			if (!checkAndExecuteMethodInfo(expression.member))
	            throw new IllegalStateException("Call target has no method info!");

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
				javaWriter.iAdd();
				break;
			case BYTE_SUB_BYTE:
			case SBYTE_SUB_SBYTE:
			case SHORT_SUB_SHORT:
			case USHORT_SUB_USHORT:
			case INT_SUB_INT:
			case UINT_SUB_UINT:
				javaWriter.iSub();
				break;
			case BYTE_MUL_BYTE:
			case SBYTE_MUL_SBYTE:
			case SHORT_MUL_SHORT:
			case USHORT_MUL_USHORT:
			case INT_MUL_INT:
			case UINT_MUL_UINT:
				javaWriter.iMul();
				break;
			case SBYTE_DIV_SBYTE:
			case SHORT_DIV_SHORT:
			case INT_DIV_INT:
				javaWriter.iDiv();
				break;
			case SBYTE_MOD_SBYTE:
			case SHORT_MOD_SHORT:
			case INT_MOD_INT:
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
				javaWriter.iAnd();
				break;
			case BYTE_OR_BYTE:
			case SBYTE_OR_SBYTE:
			case SHORT_OR_SHORT:
			case USHORT_OR_USHORT:
			case INT_OR_INT:
			case UINT_OR_UINT:
				javaWriter.iOr();
				break;
			case BYTE_XOR_BYTE:
			case SBYTE_XOR_SBYTE:
			case SHORT_XOR_SHORT:
			case USHORT_XOR_USHORT:
			case INT_XOR_INT:
			case UINT_XOR_UINT:
				javaWriter.iXor();
				break;
			case INT_SHL:
			case UINT_SHL:
				javaWriter.iShl();
				break;
			case INT_SHR:
				javaWriter.iShr();
				break;
			case INT_USHR:
			case UINT_SHR:
				javaWriter.iUShr();
				break;
			case INT_COUNT_LOW_ZEROES:
			case UINT_COUNT_LOW_ZEROES:
				javaWriter.invokeStatic(INTEGER_NUMBER_OF_TRAILING_ZEROS);
				break;
			case INT_COUNT_HIGH_ZEROES:
			case UINT_COUNT_HIGH_ZEROES:
				javaWriter.invokeStatic(INTEGER_NUMBER_OF_LEADING_ZEROS);
				break;
			case INT_COUNT_LOW_ONES:
			case UINT_COUNT_LOW_ONES:
				javaWriter.iNot();
				javaWriter.invokeStatic(INTEGER_NUMBER_OF_TRAILING_ZEROS);
				break;
			case INT_COUNT_HIGH_ONES:
			case UINT_COUNT_HIGH_ONES:
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
					RangeExpression rangeArgument = (RangeExpression)argument;
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
				Type cls = type.valueType.accept(JavaTypeVisitor.INSTANCE);
				javaWriter.checkCast(cls.getInternalName());
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
				javaWriter.arrayLoad(type.elementType.accept(JavaTypeVisitor.INSTANCE));
				break;
			}
			case ARRAY_INDEXSET: {
				ArrayTypeID type = (ArrayTypeID) expression.target.type;
				javaWriter.arrayStore(type.elementType.accept(JavaTypeVisitor.INSTANCE));
				break;
			}
			case ARRAY_INDEXGETRANGE: {
				ArrayTypeID type = (ArrayTypeID) expression.target.type;

				expression.target.accept(this);
				Expression argument = expression.arguments.arguments[0];
				if (argument instanceof RangeExpression) {
					RangeExpression rangeArgument = (RangeExpression)argument;
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
						case STRING:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_OBJECTS);
							javaWriter.checkCast("[Ljava/lang/String;");
							break;
						default:
							throw new IllegalArgumentException("Unknown basic type: " + type.elementType);
					}
				} else {
					javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_OBJECTS);
					javaWriter.checkCast("[" + type.elementType.accept(JavaTypeVisitor.INSTANCE).getDescriptor());
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
						case STRING:
							javaWriter.invokeStatic(ARRAYS_EQUALS_OBJECTS);
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
				//expression.target.accept(this);
				//for (Expression argument : expression.arguments.arguments) {
				//	argument.accept(this);
				//}
				javaWriter.invokeInterface(new JavaMethodInfo(new JavaClassInfo(expression.target.type.accept(JavaTypeVisitor.INSTANCE).getInternalName()), "accept", CompilerUtils.calcSign(expression.instancedHeader, false), Opcodes.ACC_PUBLIC));
				break;
				//throw new UnsupportedOperationException("Not yet supported!");
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
			if (!checkAndExecuteMethodInfo(expression.member))
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
				javaWriter.invokeStatic(INTEGER_PARSE_UNSIGNED);
				break;
			case UINT_PARSE_WITH_BASE:
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
			if (!checkAndExecuteByteCodeImplementation(expression.member) && !checkAndExecuteMethodInfo(expression.member))
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
        javaWriter.newObject(NullPointerException.class);
        javaWriter.dup();
        javaWriter.constant("Tried to convert a null value to nonnull type " + expression.type.accept(JavaTypeClassVisitor.INSTANCE).getSimpleName());
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
				JavaClassInfo cls = type.definition.getTag(JavaClassInfo.class);
				javaWriter.invokeStatic(new JavaMethodInfo(cls, "values", "()[L" + cls.internalClassName + ";", PUBLIC_STATIC));
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
    public Void visitConstructorThisCall(ConstructorThisCallExpression expression) {
        Type type = expression.objectType.accept(JavaTypeVisitor.INSTANCE);
		try {
			type.getInternalName();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}

        javaWriter.loadObject(0);
        if (javaWriter.method.javaClass.isEnum) {
            javaWriter.loadObject(1);
            javaWriter.loadInt(2);
        }

        for (Expression argument : expression.arguments.arguments) {
            argument.accept(this);
        }
		String internalName = type.getInternalName();
        javaWriter.invokeSpecial(internalName, "<init>", CompilerUtils.calcDesc(expression.constructor.getHeader(), javaWriter.method.javaClass.isEnum));
        return null;
    }

    @Override
    public Void visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
        javaWriter.loadObject(0);
        for (Expression argument : expression.arguments.arguments) {
            argument.accept(this);
        }
        //No super calls in enums possible, and that's already handled in the enum constructor itself.
        javaWriter.invokeSpecial(expression.objectType.accept(JavaTypeClassVisitor.INSTANCE), "<init>", CompilerUtils.calcDesc(expression.constructor.getHeader(), false));

        CompilerUtils.writeDefaultFieldInitializers(javaWriter, javaWriter.forDefinition, false);
        return null;
    }

    @Override
    public Void visitEnumConstant(EnumConstantExpression expression) {
        final Type type = expression.type.accept(JavaTypeVisitor.INSTANCE);
        javaWriter.getStaticField(type.getInternalName(), expression.value.name, type.getDescriptor());
        return null;
    }

    @Override
    public Void visitFunction(FunctionExpression expression) {
		CompilerUtils.tagMethodParameters(expression.header, false);

        if (expression.header.parameters.length == 0 && expression.body instanceof ReturnStatement && expression.body.hasTag(MatchExpression.class) && expression.closure.captures.isEmpty()) {
            ((ReturnStatement) expression.body).value.accept(this);
            return null;
        }
        final String signature = CompilerUtils.calcSign(expression.header, false);
        //final String name = CompilerUtils.getLambdaInterface(expression.header);
		final String name = CompilerUtils.getLambdaCounter();

        final JavaMethodInfo methodInfo = new JavaMethodInfo(javaWriter.method.javaClass, "accept", signature, Opcodes.ACC_PUBLIC);
		final ClassWriter lambdaCW = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		lambdaCW.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, name, null, "java/lang/Object", new String[]{CompilerUtils.getLambdaInterface(expression.header)});
		final JavaWriter functionWriter = new JavaWriter(lambdaCW, methodInfo, null, signature, null, "java/lang/Override");



		javaWriter.newObject(name);
		javaWriter.dup();

		final String constructorDesc = calcFunctionSignature(expression.closure);


		final JavaWriter constructorWriter = new JavaWriter(lambdaCW, new JavaMethodInfo(javaWriter.method.javaClass, "<init>", constructorDesc, Opcodes.ACC_PUBLIC), null, null, null);
		constructorWriter.start();
		constructorWriter.loadObject(0);
		constructorWriter.dup();
		constructorWriter.invokeSpecial(Object.class, "<init>", "()V");

		int i = 0;
		for (CapturedExpression capture : expression.closure.captures) {
			constructorWriter.dup();
			final Type type = capture.type.accept(JavaTypeVisitor.INSTANCE);
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


		final JavaStatementVisitor CSV = new JavaStatementVisitor(new JavaExpressionVisitor(functionWriter) {
			//@Override
			public Void visitGetLocalVariable(GetLocalVariableExpression varExpression) {
				final int position = calculateMemberPosition(varExpression, expression) ;
				if (position < 0)
					throw new CompileException(varExpression.position, CompileExceptionCode.INTERNAL_ERROR, "Captured Statement error");
				functionWriter.loadObject(0);
				functionWriter.getField(name, "captured" + position, varExpression.variable.type.accept(JavaTypeVisitor.INSTANCE).getDescriptor());
				return null;
			}
		});


		expression.body.accept(CSV);


        functionWriter.ret();



		functionWriter.end();
        lambdaCW.visitEnd();

        JavaModule.classes.putIfAbsent(name, lambdaCW.toByteArray());

        try (FileOutputStream out = new FileOutputStream(name + ".class")){
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
        return -1;
    }

    private String calcFunctionSignature(LambdaClosure closure) {
        StringJoiner joiner = new StringJoiner("", "(", ")V");

        for (CapturedExpression capture : closure.captures) {
            String descriptor = capture.type.accept(JavaTypeVisitor.INSTANCE).getDescriptor();
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

        if(parameter == null) {
			System.err.println("NULL PARAMETER!!!");
			//FIXME
			parameter = new JavaParameterInfo(1, Type.INT_TYPE);
		}

        javaWriter.load(expression.parameter.type.accept(JavaTypeVisitor.INSTANCE), parameter.index);
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
			if (!checkAndExecuteMethodInfo(expression.getter))
	            throw new IllegalStateException("Call target has no method info!");

			return null;
		}

		switch (builtin) {
			case INT_HIGHEST_ONE_BIT:
			case UINT_HIGHEST_ONE_BIT:
				javaWriter.invokeStatic(INTEGER_HIGHEST_ONE_BIT);
				break;
			case INT_LOWEST_ONE_BIT:
			case UINT_LOWEST_ONE_BIT:
				javaWriter.invokeStatic(INTEGER_LOWEST_ONE_BIT);
				break;
			case INT_HIGHEST_ZERO_BIT:
			case UINT_HIGHEST_ZERO_BIT:
				javaWriter.iNeg();
				javaWriter.invokeStatic(INTEGER_HIGHEST_ONE_BIT);
				break;
			case INT_LOWEST_ZERO_BIT:
			case UINT_LOWEST_ZERO_BIT:
				javaWriter.iNeg();
				javaWriter.invokeStatic(INTEGER_LOWEST_ONE_BIT);
				break;
			case INT_BIT_COUNT:
			case UINT_BIT_COUNT:
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
				ArrayTypeID result = new ArrayTypeID(null, type.keyType, 1);
				Type resultType = result.accept(JavaTypeVisitor.INSTANCE);

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
				ArrayTypeID result = new ArrayTypeID(null, type.valueType, 1);
				Type resultType = result.accept(JavaTypeVisitor.INSTANCE);

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
						case STRING:
							javaWriter.invokeStatic(ARRAYS_DEEPHASHCODE);
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
			case RANGE_FROM:
				// TODO: range types
				javaWriter.getField(IntRange.class, "from", int.class);
				break;
			case RANGE_TO:
				// TODO: range types
				javaWriter.getField(IntRange.class, "to", int.class);
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
        javaWriter.checkCast(expression.type.accept(JavaTypeVisitor.INSTANCE));
        return null;
    }

    @Override
    public Void visitIs(IsExpression expression) {
        expression.value.accept(this);
        javaWriter.instanceOf(expression.isType.accept(JavaTypeVisitor.INSTANCE));
        return null;
    }

    @Override
    public Void visitMakeConst(MakeConstExpression expression) {
        return null;
    }

    @Override
    public Void visitMap(MapExpression expression) {
        javaWriter.newObject(expression.type.accept(JavaTypeClassVisitor.INSTANCE));
        javaWriter.dup();
        javaWriter.invokeSpecial("java/util/Map", "<init>", "()V");
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
        if (expression.value.type == BasicTypeID.STRING)
            javaWriter.invokeVirtual(new JavaMethodInfo(new JavaClassInfo("java/lang/Object"), "hashCode", "()I", 0));

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
        }

        javaWriter.label(end);


        //throw new UnsupportedOperationException("Not yet implemented!");
        return null;
    }

    private static boolean hasNoDefault(MatchExpression switchStatement) {
        for (MatchExpression.Case switchCase : switchStatement.cases)
            if (switchCase.key == null) return false;
        return true;
    }

    @Override
    public Void visitNew(NewExpression expression) {
        final String type;
        if (expression.type instanceof DefinitionTypeID)
            type = ((DefinitionTypeID) expression.type).definition.name;
        else
            type = Type.getDescriptor(expression.type.accept(JavaTypeClassVisitor.INSTANCE));

        javaWriter.newObject(type);
        javaWriter.dup();
        StringBuilder signatureBuilder = new StringBuilder("(");
        for (Expression argument : expression.arguments.arguments) {
            argument.accept(this);
            signatureBuilder.append(Type.getDescriptor(argument.type.accept(JavaTypeClassVisitor.INSTANCE)));
        }
        signatureBuilder.append(")V");
        javaWriter.invokeSpecial(type, "<init>", signatureBuilder.toString());

        return null;
    }

    @Override
    public Void visitNull(NullExpression expression) {
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
		// TODO: compile to: throw new AssertionError(expression.value)
		throw new UnsupportedOperationException("Not yet supported");
	}

	@Override
	public Void visitPostCall(PostCallExpression expression) {
		expression.target.accept(this);
		javaWriter.dup(expression.type.accept(new JavaTypeVisitor()));
        if (!checkAndExecuteByteCodeImplementation(expression.member) && !checkAndExecuteMethodInfo(expression.member))
            throw new IllegalStateException("Call target has no method info!");

        return null;
    }

    @Override
    public Void visitRange(RangeExpression expression) {
        // TODO: there are other kinds of ranges also; there should be a Range<T, T> type with creation of synthetic types
        if (expression.from.type.accept(JavaTypeClassVisitor.INSTANCE) != int.class)
            throw new CompileException(expression.position, CompileExceptionCode.INTERNAL_ERROR, "Only integer ranges supported");

        javaWriter.newObject(IntRange.class);
        javaWriter.dup();
        expression.from.accept(this);
        expression.to.accept(this);
        System.out.println(IntRange.class.getName());
        javaWriter.invokeSpecial("org/openzen/zenscript/implementations/IntRange", "<init>", "(II)V");

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
        javaWriter.store(expression.type.accept(JavaTypeVisitor.INSTANCE), parameter.index);
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
			if (!checkAndExecuteMethodInfo(expression.getter))
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
				JavaClassInfo cls = type.definition.getTag(JavaClassInfo.class);
				javaWriter.invokeStatic(new JavaMethodInfo(cls, "values", "()[L" + cls.internalClassName + ";", PUBLIC_STATIC));
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown builtin: " + builtin);
		}

		throw new UnsupportedOperationException("Unknown builtin: " + builtin);
    }

    @Override
    public Void visitStaticSetter(StaticSetterExpression expression) {
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Void visitWrapOptional(WrapOptionalExpression expression) {
        // TODO: convert basic types (char, int, float, ...) to their boxed (Character, Integer, Float, ...) counterparts
        // -- any object type values can just be passed as-is
        expression.value.accept(this);
        return null;
    }

    public JavaWriter getJavaWriter() {
        return javaWriter;
    }


    //Will return true if a JavaBytecodeImplementation.class tag exists, and will compile that tag
    private boolean checkAndExecuteByteCodeImplementation(DefinitionMemberRef member) {
        JavaBytecodeImplementation implementation = member.getTag(JavaBytecodeImplementation.class);
        if (implementation != null) {
            implementation.compile(getJavaWriter());
            return true;
        }
        return false;
    }

    //Will return true if a JavaMethodInfo.class tag exists, and will compile that tag
    private boolean checkAndExecuteMethodInfo(DefinitionMemberRef member) {
        JavaMethodInfo methodInfo = member.getTag(JavaMethodInfo.class);
        if (methodInfo == null)
            return false;

        if (methodInfo.isStatic()) {
            getJavaWriter().invokeStatic(methodInfo);
        } else {
            getJavaWriter().invokeVirtual(methodInfo);
        }
        return true;
    }


    //Will return true if a JavaFieldInfo.class tag exists, and will compile that tag
    public boolean checkAndPutFieldInfo(FieldMemberRef field, boolean isStatic) {
		JavaFieldInfo fieldInfo = field.getTag(JavaFieldInfo.class);
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
        JavaFieldInfo fieldInfo = field.getTag(JavaFieldInfo.class);
        if (fieldInfo == null)
            return false;

        getJavaWriter().getStaticField(fieldInfo);
        return true;
    }

    public boolean checkAndGetFieldInfo(FieldMemberRef field, boolean isStatic) {
        JavaFieldInfo fieldInfo = field.getTag(JavaFieldInfo.class);
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
