package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

public class BasicTypeMembers {
	private static final MemberSet NO_MEMBERS = new MemberSet();

	public static MemberSet get(BasicTypeID type) {
		switch (type) {
			case VOID:
				return NO_MEMBERS;
			case NULL:
				return NO_MEMBERS;
			case BOOL:
				return getBool();
			case BYTE:
				return getByte();
			case SBYTE:
				return getSByte();
			case SHORT:
				return getShort();
			case USHORT:
				return getUShort();
			case INT:
				return getInt();
			case UINT:
				return getUInt();
			case LONG:
				return getLong();
			case ULONG:
				return getULong();
			case USIZE:
				return getUSize();
			case FLOAT:
				return getFloat();
			case DOUBLE:
				return getDouble();
			case CHAR:
				return getChar();
			case STRING:
				return getString();
			case UNDETERMINED:
				return NO_MEMBERS;
			case INVALID:
				return NO_MEMBERS;
			default:
				throw new IllegalArgumentException();
		}
	}

	private static MemberSet getBool() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.NOT, new MethodInstance(BuiltinMethodSymbol.BOOL_NOT));
		builder.operator(OperatorType.AND, new MethodInstance(BuiltinMethodSymbol.BOOL_AND));
		builder.operator(OperatorType.OR, new MethodInstance(BuiltinMethodSymbol.BOOL_OR));
		builder.operator(OperatorType.XOR, new MethodInstance(BuiltinMethodSymbol.BOOL_XOR));
		builder.operator(OperatorType.EQUALS, new MethodInstance(BuiltinMethodSymbol.BOOL_EQUALS));
		builder.operator(OperatorType.NOTEQUALS, new MethodInstance(BuiltinMethodSymbol.BOOL_NOTEQUALS));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.BOOL_TO_STRING));
		builder.staticMethod(new MethodInstance(BuiltinMethodSymbol.BOOL_PARSE));
		return builder.build();
	}

	private static MemberSet getByte() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.INVERT, new MethodInstance(BuiltinMethodSymbol.BYTE_INVERT));
		builder.operator(OperatorType.INCREMENT, new MethodInstance(BuiltinMethodSymbol.BYTE_INC));
		builder.operator(OperatorType.DECREMENT, new MethodInstance(BuiltinMethodSymbol.BYTE_DEC));
		builder.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.BYTE_ADD_BYTE));
		builder.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.BYTE_SUB_BYTE));
		builder.operator(OperatorType.MUL, new MethodInstance(BuiltinMethodSymbol.BYTE_MUL_BYTE));
		builder.operator(OperatorType.DIV, new MethodInstance(BuiltinMethodSymbol.BYTE_DIV_BYTE));
		builder.operator(OperatorType.MOD, new MethodInstance(BuiltinMethodSymbol.BYTE_MOD_BYTE));
		builder.operator(OperatorType.AND, new MethodInstance(BuiltinMethodSymbol.BYTE_AND_BYTE));
		builder.operator(OperatorType.OR, new MethodInstance(BuiltinMethodSymbol.BYTE_OR_BYTE));
		builder.operator(OperatorType.XOR, new MethodInstance(BuiltinMethodSymbol.BYTE_XOR_BYTE));
		builder.operator(OperatorType.SHL, new MethodInstance(BuiltinMethodSymbol.BYTE_SHL));
		builder.operator(OperatorType.SHR, new MethodInstance(BuiltinMethodSymbol.BYTE_SHR));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.BYTE_COMPARE));

		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_SBYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_SHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_USHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_INT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_UINT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_LONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_ULONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_USIZE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_FLOAT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_DOUBLE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_CHAR));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_STRING));

		builder.method(new MethodInstance(BuiltinMethodSymbol.BYTE_PARSE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.BYTE_PARSE_WITH_BASE));

		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.BYTE_GET_MIN_VALUE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.BYTE_GET_MAX_VALUE));

		return builder.build();
	}

	private static MemberSet getSByte() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.INVERT, new MethodInstance(BuiltinMethodSymbol.SBYTE_INVERT));
		builder.operator(OperatorType.INCREMENT, new MethodInstance(BuiltinMethodSymbol.SBYTE_INC));
		builder.operator(OperatorType.DECREMENT, new MethodInstance(BuiltinMethodSymbol.SBYTE_DEC));
		builder.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.SBYTE_ADD_SBYTE));
		builder.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.SBYTE_SUB_SBYTE));
		builder.operator(OperatorType.MUL, new MethodInstance(BuiltinMethodSymbol.SBYTE_MUL_SBYTE));
		builder.operator(OperatorType.DIV, new MethodInstance(BuiltinMethodSymbol.SBYTE_DIV_SBYTE));
		builder.operator(OperatorType.MOD, new MethodInstance(BuiltinMethodSymbol.SBYTE_MOD_SBYTE));
		builder.operator(OperatorType.AND, new MethodInstance(BuiltinMethodSymbol.SBYTE_AND_SBYTE));
		builder.operator(OperatorType.OR, new MethodInstance(BuiltinMethodSymbol.SBYTE_OR_SBYTE));
		builder.operator(OperatorType.XOR, new MethodInstance(BuiltinMethodSymbol.SBYTE_XOR_SBYTE));
		builder.operator(OperatorType.SHL, new MethodInstance(BuiltinMethodSymbol.SBYTE_SHL));
		builder.operator(OperatorType.SHR, new MethodInstance(BuiltinMethodSymbol.SBYTE_SHR));
		builder.operator(OperatorType.USHR, new MethodInstance(BuiltinMethodSymbol.SBYTE_USHR));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.SBYTE_COMPARE));

		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_BYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_SHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_USHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_INT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_UINT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_LONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_ULONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_USIZE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_FLOAT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_DOUBLE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_CHAR));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_STRING));

		builder.method(new MethodInstance(BuiltinMethodSymbol.SBYTE_PARSE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.SBYTE_PARSE_WITH_BASE));

		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.SBYTE_GET_MIN_VALUE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.SBYTE_GET_MAX_VALUE));
		return builder.build();
	}

	private static MemberSet getShort() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.INVERT, new MethodInstance(BuiltinMethodSymbol.SHORT_INVERT));
		builder.operator(OperatorType.INCREMENT, new MethodInstance(BuiltinMethodSymbol.SHORT_INC));
		builder.operator(OperatorType.DECREMENT, new MethodInstance(BuiltinMethodSymbol.SHORT_DEC));
		builder.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.SHORT_ADD_SHORT));
		builder.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.SHORT_SUB_SHORT));
		builder.operator(OperatorType.MUL, new MethodInstance(BuiltinMethodSymbol.SHORT_MUL_SHORT));
		builder.operator(OperatorType.DIV, new MethodInstance(BuiltinMethodSymbol.SHORT_DIV_SHORT));
		builder.operator(OperatorType.MOD, new MethodInstance(BuiltinMethodSymbol.SHORT_MOD_SHORT));
		builder.operator(OperatorType.AND, new MethodInstance(BuiltinMethodSymbol.SHORT_AND_SHORT));
		builder.operator(OperatorType.OR, new MethodInstance(BuiltinMethodSymbol.SHORT_OR_SHORT));
		builder.operator(OperatorType.XOR, new MethodInstance(BuiltinMethodSymbol.SHORT_XOR_SHORT));
		builder.operator(OperatorType.SHL, new MethodInstance(BuiltinMethodSymbol.SHORT_SHL));
		builder.operator(OperatorType.SHR, new MethodInstance(BuiltinMethodSymbol.SHORT_SHR));
		builder.operator(OperatorType.USHR, new MethodInstance(BuiltinMethodSymbol.SHORT_USHR));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.SHORT_COMPARE));

		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_BYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_SBYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_USHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_INT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_UINT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_LONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_ULONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_USIZE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_FLOAT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_DOUBLE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_CHAR));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_STRING));

		builder.method(new MethodInstance(BuiltinMethodSymbol.SHORT_PARSE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.SHORT_PARSE_WITH_BASE));

		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.SHORT_GET_MIN_VALUE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.SHORT_GET_MAX_VALUE));
		return builder.build();
	}

	private static MemberSet getUShort() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.INVERT, new MethodInstance(BuiltinMethodSymbol.USHORT_INVERT));
		builder.operator(OperatorType.INCREMENT, new MethodInstance(BuiltinMethodSymbol.USHORT_INC));
		builder.operator(OperatorType.DECREMENT, new MethodInstance(BuiltinMethodSymbol.USHORT_DEC));
		builder.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.USHORT_ADD_USHORT));
		builder.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.USHORT_SUB_USHORT));
		builder.operator(OperatorType.MUL, new MethodInstance(BuiltinMethodSymbol.USHORT_MUL_USHORT));
		builder.operator(OperatorType.DIV, new MethodInstance(BuiltinMethodSymbol.USHORT_DIV_USHORT));
		builder.operator(OperatorType.MOD, new MethodInstance(BuiltinMethodSymbol.USHORT_MOD_USHORT));
		builder.operator(OperatorType.AND, new MethodInstance(BuiltinMethodSymbol.USHORT_AND_USHORT));
		builder.operator(OperatorType.OR, new MethodInstance(BuiltinMethodSymbol.USHORT_OR_USHORT));
		builder.operator(OperatorType.XOR, new MethodInstance(BuiltinMethodSymbol.USHORT_XOR_USHORT));
		builder.operator(OperatorType.SHL, new MethodInstance(BuiltinMethodSymbol.USHORT_SHL));
		builder.operator(OperatorType.SHR, new MethodInstance(BuiltinMethodSymbol.USHORT_SHR));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.USHORT_COMPARE));

		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_BYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_SBYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_SHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_INT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_UINT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_LONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_ULONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_USIZE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_FLOAT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_DOUBLE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_CHAR));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_STRING));

		builder.method(new MethodInstance(BuiltinMethodSymbol.USHORT_PARSE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.USHORT_PARSE_WITH_BASE));

		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.USHORT_GET_MIN_VALUE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.USHORT_GET_MAX_VALUE));
		return builder.build();
	}

	private static MemberSet getInt() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.INVERT, new MethodInstance(BuiltinMethodSymbol.INT_INVERT));
		builder.operator(OperatorType.NEG, new MethodInstance(BuiltinMethodSymbol.INT_NEG));
		builder.operator(OperatorType.INCREMENT, new MethodInstance(BuiltinMethodSymbol.INT_INC));
		builder.operator(OperatorType.DECREMENT, new MethodInstance(BuiltinMethodSymbol.INT_DEC));
		builder.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.INT_ADD_INT));
		builder.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.INT_ADD_USIZE));
		builder.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.INT_SUB_INT));
		builder.operator(OperatorType.MUL, new MethodInstance(BuiltinMethodSymbol.INT_MUL_INT));
		builder.operator(OperatorType.DIV, new MethodInstance(BuiltinMethodSymbol.INT_DIV_INT));
		builder.operator(OperatorType.MOD, new MethodInstance(BuiltinMethodSymbol.INT_MOD_INT));
		builder.operator(OperatorType.AND, new MethodInstance(BuiltinMethodSymbol.INT_AND_INT));
		builder.operator(OperatorType.OR, new MethodInstance(BuiltinMethodSymbol.INT_OR_INT));
		builder.operator(OperatorType.XOR, new MethodInstance(BuiltinMethodSymbol.INT_XOR_INT));
		builder.operator(OperatorType.SHL, new MethodInstance(BuiltinMethodSymbol.INT_SHL));
		builder.operator(OperatorType.SHR, new MethodInstance(BuiltinMethodSymbol.INT_SHR));
		builder.operator(OperatorType.USHR, new MethodInstance(BuiltinMethodSymbol.INT_USHR));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.INT_COMPARE));

		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.INT_TO_BYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.INT_TO_SBYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.INT_TO_SHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.INT_TO_USHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.INT_TO_UINT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.INT_TO_LONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.INT_TO_ULONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.INT_TO_USIZE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.INT_TO_FLOAT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.INT_TO_DOUBLE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.INT_TO_CHAR));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.INT_TO_STRING));

		builder.method(new MethodInstance(BuiltinMethodSymbol.INT_PARSE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.INT_PARSE_WITH_BASE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.INT_GET_MIN_VALUE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.INT_GET_MAX_VALUE));

		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_COUNT_LOW_ZEROES));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_COUNT_HIGH_ZEROES));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_COUNT_LOW_ONES));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_COUNT_HIGH_ONES));

		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_HIGHEST_ONE_BIT));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_LOWEST_ONE_BIT));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_HIGHEST_ZERO_BIT));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_LOWEST_ZERO_BIT));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_BIT_COUNT));
		return builder.build();
	}

	private static MemberSet getUInt() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.INVERT, new MethodInstance(BuiltinMethodSymbol.UINT_INVERT));
		builder.operator(OperatorType.INCREMENT, new MethodInstance(BuiltinMethodSymbol.UINT_INC));
		builder.operator(OperatorType.DECREMENT, new MethodInstance(BuiltinMethodSymbol.UINT_DEC));
		builder.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.UINT_ADD_UINT));
		builder.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.UINT_SUB_UINT));
		builder.operator(OperatorType.MUL, new MethodInstance(BuiltinMethodSymbol.UINT_MUL_UINT));
		builder.operator(OperatorType.DIV, new MethodInstance(BuiltinMethodSymbol.UINT_DIV_UINT));
		builder.operator(OperatorType.MOD, new MethodInstance(BuiltinMethodSymbol.UINT_MOD_UINT));
		builder.operator(OperatorType.AND, new MethodInstance(BuiltinMethodSymbol.UINT_AND_UINT));
		builder.operator(OperatorType.OR, new MethodInstance(BuiltinMethodSymbol.UINT_OR_UINT));
		builder.operator(OperatorType.XOR, new MethodInstance(BuiltinMethodSymbol.UINT_XOR_UINT));
		builder.operator(OperatorType.SHL, new MethodInstance(BuiltinMethodSymbol.UINT_SHL));
		builder.operator(OperatorType.SHR, new MethodInstance(BuiltinMethodSymbol.UINT_SHR));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.UINT_COMPARE));

		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_BYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_SBYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_SHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_USHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_INT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_LONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_ULONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_USIZE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_FLOAT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_DOUBLE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_CHAR));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_STRING));

		builder.method("parse", new MethodInstance(BuiltinMethodSymbol.UINT_PARSE));
		builder.method("parse", new MethodInstance(BuiltinMethodSymbol.UINT_PARSE_WITH_BASE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.UINT_GET_MIN_VALUE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.UINT_GET_MAX_VALUE));

		builder.getter("countLowZeroes", new MethodInstance(BuiltinMethodSymbol.UINT_COUNT_LOW_ZEROES));
		builder.getter("countHighZeroes", new MethodInstance(BuiltinMethodSymbol.UINT_COUNT_HIGH_ZEROES));
		builder.getter("countLowOnes", new MethodInstance(BuiltinMethodSymbol.UINT_COUNT_LOW_ONES));
		builder.getter("countHighOnes", new MethodInstance(BuiltinMethodSymbol.UINT_COUNT_HIGH_ONES));

		builder.getter("highestOneBit", new MethodInstance(BuiltinMethodSymbol.UINT_HIGHEST_ONE_BIT));
		builder.getter("LowestOneBit", new MethodInstance(BuiltinMethodSymbol.UINT_LOWEST_ONE_BIT));
		builder.getter("highestZeroBit", new MethodInstance(BuiltinMethodSymbol.UINT_HIGHEST_ZERO_BIT));
		builder.getter("lowestZeroBit", new MethodInstance(BuiltinMethodSymbol.UINT_LOWEST_ZERO_BIT));
		builder.getter("bitCount", new MethodInstance(BuiltinMethodSymbol.UINT_BIT_COUNT));
		return builder.build();
	}

	private static MemberSet getLong() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.INVERT, new MethodInstance(BuiltinMethodSymbol.LONG_INVERT));
		builder.operator(OperatorType.INCREMENT, new MethodInstance(BuiltinMethodSymbol.LONG_INC));
		builder.operator(OperatorType.DECREMENT, new MethodInstance(BuiltinMethodSymbol.LONG_DEC));
		builder.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.LONG_ADD_LONG));
		builder.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.LONG_SUB_LONG));
		builder.operator(OperatorType.MUL, new MethodInstance(BuiltinMethodSymbol.LONG_MUL_LONG));
		builder.operator(OperatorType.DIV, new MethodInstance(BuiltinMethodSymbol.LONG_DIV_LONG));
		builder.operator(OperatorType.MOD, new MethodInstance(BuiltinMethodSymbol.LONG_MOD_LONG));
		builder.operator(OperatorType.AND, new MethodInstance(BuiltinMethodSymbol.LONG_AND_LONG));
		builder.operator(OperatorType.OR, new MethodInstance(BuiltinMethodSymbol.LONG_OR_LONG));
		builder.operator(OperatorType.XOR, new MethodInstance(BuiltinMethodSymbol.LONG_XOR_LONG));
		builder.operator(OperatorType.SHL, new MethodInstance(BuiltinMethodSymbol.LONG_SHL));
		builder.operator(OperatorType.SHR, new MethodInstance(BuiltinMethodSymbol.LONG_SHR));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.LONG_COMPARE));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.LONG_COMPARE_INT));

		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_BYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_SBYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_SHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_USHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_INT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_UINT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_ULONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_USIZE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_FLOAT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_DOUBLE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_CHAR));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_STRING));

		builder.method("parse", new MethodInstance(BuiltinMethodSymbol.LONG_PARSE));
		builder.method("parse", new MethodInstance(BuiltinMethodSymbol.LONG_PARSE_WITH_BASE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.LONG_GET_MIN_VALUE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.LONG_GET_MAX_VALUE));

		builder.getter("countLowZeroes", new MethodInstance(BuiltinMethodSymbol.LONG_COUNT_LOW_ZEROES));
		builder.getter("countHighZeroes", new MethodInstance(BuiltinMethodSymbol.LONG_COUNT_HIGH_ZEROES));
		builder.getter("countLowOnes", new MethodInstance(BuiltinMethodSymbol.LONG_COUNT_LOW_ONES));
		builder.getter("countHighOnes", new MethodInstance(BuiltinMethodSymbol.LONG_COUNT_HIGH_ONES));

		builder.getter("highestOneBit", new MethodInstance(BuiltinMethodSymbol.LONG_HIGHEST_ONE_BIT));
		builder.getter("LowestOneBit", new MethodInstance(BuiltinMethodSymbol.LONG_LOWEST_ONE_BIT));
		builder.getter("highestZeroBit", new MethodInstance(BuiltinMethodSymbol.LONG_HIGHEST_ZERO_BIT));
		builder.getter("lowestZeroBit", new MethodInstance(BuiltinMethodSymbol.LONG_LOWEST_ZERO_BIT));
		builder.getter("bitCount", new MethodInstance(BuiltinMethodSymbol.LONG_BIT_COUNT));
		return builder.build();
	}

	private static MemberSet getULong() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.INVERT, new MethodInstance(BuiltinMethodSymbol.ULONG_INVERT));
		builder.operator(OperatorType.INCREMENT, new MethodInstance(BuiltinMethodSymbol.ULONG_INC));
		builder.operator(OperatorType.DECREMENT, new MethodInstance(BuiltinMethodSymbol.ULONG_DEC));
		builder.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.ULONG_ADD_ULONG));
		builder.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.ULONG_SUB_ULONG));
		builder.operator(OperatorType.MUL, new MethodInstance(BuiltinMethodSymbol.ULONG_MUL_ULONG));
		builder.operator(OperatorType.DIV, new MethodInstance(BuiltinMethodSymbol.ULONG_DIV_ULONG));
		builder.operator(OperatorType.MOD, new MethodInstance(BuiltinMethodSymbol.ULONG_MOD_ULONG));
		builder.operator(OperatorType.AND, new MethodInstance(BuiltinMethodSymbol.ULONG_AND_ULONG));
		builder.operator(OperatorType.OR, new MethodInstance(BuiltinMethodSymbol.ULONG_OR_ULONG));
		builder.operator(OperatorType.XOR, new MethodInstance(BuiltinMethodSymbol.ULONG_XOR_ULONG));
		builder.operator(OperatorType.SHL, new MethodInstance(BuiltinMethodSymbol.ULONG_SHL));
		builder.operator(OperatorType.SHR, new MethodInstance(BuiltinMethodSymbol.ULONG_SHR));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.ULONG_COMPARE));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.ULONG_COMPARE_UINT));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.ULONG_COMPARE_USIZE));

		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_BYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_SBYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_SHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_USHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_INT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_UINT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_LONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_USIZE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_FLOAT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_DOUBLE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_CHAR));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_STRING));

		builder.method("parse", new MethodInstance(BuiltinMethodSymbol.ULONG_PARSE));
		builder.method("parse", new MethodInstance(BuiltinMethodSymbol.ULONG_PARSE_WITH_BASE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.ULONG_GET_MIN_VALUE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.ULONG_GET_MAX_VALUE));

		builder.getter("countLowZeroes", new MethodInstance(BuiltinMethodSymbol.ULONG_COUNT_LOW_ZEROES));
		builder.getter("countHighZeroes", new MethodInstance(BuiltinMethodSymbol.ULONG_COUNT_HIGH_ZEROES));
		builder.getter("countLowOnes", new MethodInstance(BuiltinMethodSymbol.ULONG_COUNT_LOW_ONES));
		builder.getter("countHighOnes", new MethodInstance(BuiltinMethodSymbol.ULONG_COUNT_HIGH_ONES));

		builder.getter("highestOneBit", new MethodInstance(BuiltinMethodSymbol.ULONG_HIGHEST_ONE_BIT));
		builder.getter("LowestOneBit", new MethodInstance(BuiltinMethodSymbol.ULONG_LOWEST_ONE_BIT));
		builder.getter("highestZeroBit", new MethodInstance(BuiltinMethodSymbol.ULONG_HIGHEST_ZERO_BIT));
		builder.getter("lowestZeroBit", new MethodInstance(BuiltinMethodSymbol.ULONG_LOWEST_ZERO_BIT));
		builder.getter("bitCount", new MethodInstance(BuiltinMethodSymbol.ULONG_BIT_COUNT));
		return builder.build();
	}

	private static MemberSet getUSize() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.INVERT, new MethodInstance(BuiltinMethodSymbol.USIZE_INVERT));
		builder.operator(OperatorType.INCREMENT, new MethodInstance(BuiltinMethodSymbol.USIZE_INC));
		builder.operator(OperatorType.DECREMENT, new MethodInstance(BuiltinMethodSymbol.USIZE_DEC));
		builder.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.USIZE_ADD_USIZE));
		builder.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.USIZE_SUB_USIZE));
		builder.operator(OperatorType.MUL, new MethodInstance(BuiltinMethodSymbol.USIZE_MUL_USIZE));
		builder.operator(OperatorType.DIV, new MethodInstance(BuiltinMethodSymbol.USIZE_DIV_USIZE));
		builder.operator(OperatorType.MOD, new MethodInstance(BuiltinMethodSymbol.USIZE_MOD_USIZE));
		builder.operator(OperatorType.AND, new MethodInstance(BuiltinMethodSymbol.USIZE_AND_USIZE));
		builder.operator(OperatorType.OR, new MethodInstance(BuiltinMethodSymbol.USIZE_OR_USIZE));
		builder.operator(OperatorType.XOR, new MethodInstance(BuiltinMethodSymbol.USIZE_XOR_USIZE));
		builder.operator(OperatorType.SHL, new MethodInstance(BuiltinMethodSymbol.USIZE_SHL));
		builder.operator(OperatorType.SHR, new MethodInstance(BuiltinMethodSymbol.USIZE_SHR));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.USIZE_COMPARE));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.USIZE_COMPARE_UINT));

		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_BYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_SBYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_SHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_USHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_INT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_UINT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_LONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_ULONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_FLOAT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_DOUBLE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_CHAR));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_STRING));

		builder.method("parse", new MethodInstance(BuiltinMethodSymbol.USIZE_PARSE));
		builder.method("parse", new MethodInstance(BuiltinMethodSymbol.USIZE_PARSE_WITH_BASE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.USIZE_GET_MIN_VALUE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.USIZE_GET_MAX_VALUE));

		builder.getter("countLowZeroes", new MethodInstance(BuiltinMethodSymbol.USIZE_COUNT_LOW_ZEROES));
		builder.getter("countHighZeroes", new MethodInstance(BuiltinMethodSymbol.USIZE_COUNT_HIGH_ZEROES));
		builder.getter("countLowOnes", new MethodInstance(BuiltinMethodSymbol.USIZE_COUNT_LOW_ONES));
		builder.getter("countHighOnes", new MethodInstance(BuiltinMethodSymbol.USIZE_COUNT_HIGH_ONES));

		builder.getter("highestOneBit", new MethodInstance(BuiltinMethodSymbol.USIZE_HIGHEST_ONE_BIT));
		builder.getter("LowestOneBit", new MethodInstance(BuiltinMethodSymbol.USIZE_LOWEST_ONE_BIT));
		builder.getter("highestZeroBit", new MethodInstance(BuiltinMethodSymbol.USIZE_HIGHEST_ZERO_BIT));
		builder.getter("lowestZeroBit", new MethodInstance(BuiltinMethodSymbol.USIZE_LOWEST_ZERO_BIT));
		builder.getter("bitCount", new MethodInstance(BuiltinMethodSymbol.USIZE_BIT_COUNT));
		builder.getter("bits", new MethodInstance(BuiltinMethodSymbol.USIZE_BITS));
		return builder.build();
	}

	private static MemberSet getFloat() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.INVERT, new MethodInstance(BuiltinMethodSymbol.FLOAT_INVERT));
		builder.operator(OperatorType.INCREMENT, new MethodInstance(BuiltinMethodSymbol.FLOAT_INC));
		builder.operator(OperatorType.DECREMENT, new MethodInstance(BuiltinMethodSymbol.FLOAT_DEC));
		builder.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.FLOAT_ADD_FLOAT));
		builder.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.FLOAT_SUB_FLOAT));
		builder.operator(OperatorType.MUL, new MethodInstance(BuiltinMethodSymbol.FLOAT_MUL_FLOAT));
		builder.operator(OperatorType.DIV, new MethodInstance(BuiltinMethodSymbol.FLOAT_DIV_FLOAT));
		builder.operator(OperatorType.MOD, new MethodInstance(BuiltinMethodSymbol.FLOAT_MOD_FLOAT));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.FLOAT_COMPARE));

		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.FLOAT_TO_BYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.FLOAT_TO_SBYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.FLOAT_TO_SHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.FLOAT_TO_USHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.FLOAT_TO_INT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.FLOAT_TO_UINT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.FLOAT_TO_LONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.FLOAT_TO_ULONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.FLOAT_TO_USIZE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.FLOAT_TO_DOUBLE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.FLOAT_TO_STRING));

		builder.getter(new MethodInstance(BuiltinMethodSymbol.FLOAT_BITS));
		builder.staticMethod(new MethodInstance(BuiltinMethodSymbol.FLOAT_FROM_BITS));
		builder.method(new MethodInstance(BuiltinMethodSymbol.FLOAT_PARSE));

		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.FLOAT_GET_MIN_VALUE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.FLOAT_GET_MAX_VALUE));

		return builder.build();
	}

	private static MemberSet getDouble() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.INVERT, new MethodInstance(BuiltinMethodSymbol.DOUBLE_INVERT));
		builder.operator(OperatorType.INCREMENT, new MethodInstance(BuiltinMethodSymbol.DOUBLE_INC));
		builder.operator(OperatorType.DECREMENT, new MethodInstance(BuiltinMethodSymbol.DOUBLE_DEC));
		builder.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.DOUBLE_ADD_DOUBLE));
		builder.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.DOUBLE_SUB_DOUBLE));
		builder.operator(OperatorType.MUL, new MethodInstance(BuiltinMethodSymbol.DOUBLE_MUL_DOUBLE));
		builder.operator(OperatorType.DIV, new MethodInstance(BuiltinMethodSymbol.DOUBLE_DIV_DOUBLE));
		builder.operator(OperatorType.MOD, new MethodInstance(BuiltinMethodSymbol.DOUBLE_MOD_DOUBLE));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.DOUBLE_COMPARE));

		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.DOUBLE_TO_BYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.DOUBLE_TO_SBYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.DOUBLE_TO_SHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.DOUBLE_TO_USHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.DOUBLE_TO_INT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.DOUBLE_TO_UINT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.DOUBLE_TO_LONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.DOUBLE_TO_ULONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.DOUBLE_TO_USIZE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.DOUBLE_TO_FLOAT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.DOUBLE_TO_STRING));

		builder.getter(new MethodInstance(BuiltinMethodSymbol.DOUBLE_BITS));
		builder.staticMethod(new MethodInstance(BuiltinMethodSymbol.DOUBLE_FROM_BITS));
		builder.method(new MethodInstance(BuiltinMethodSymbol.DOUBLE_PARSE));

		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.DOUBLE_GET_MIN_VALUE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.DOUBLE_GET_MAX_VALUE));

		return builder.build();
	}

	private static MemberSet getChar() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.CHAR_ADD_INT));
		builder.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.CHAR_SUB_INT));
		builder.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.CHAR_SUB_CHAR));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.CHAR_COMPARE));

		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.CHAR_TO_BYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.CHAR_TO_SBYTE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.CHAR_TO_SHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.CHAR_TO_USHORT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.CHAR_TO_INT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.CHAR_TO_UINT));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.CHAR_TO_LONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.CHAR_TO_ULONG));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.CHAR_TO_USIZE));
		builder.implicitCast(new MethodInstance(BuiltinMethodSymbol.CHAR_TO_STRING));

		builder.getter(new MethodInstance(BuiltinMethodSymbol.CHAR_GET_MIN_VALUE));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.CHAR_GET_MAX_VALUE));

		builder.method(new MethodInstance(BuiltinMethodSymbol.CHAR_REMOVE_DIACRITICS));
		builder.method(new MethodInstance(BuiltinMethodSymbol.CHAR_TO_LOWER_CASE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.CHAR_TO_UPPER_CASE));
		return builder.build();
	}

	private static MemberSet getString() {
		MemberSet.Builder builder = MemberSet.create();
		builder.constructor(new MethodInstance(BuiltinMethodSymbol.STRING_CONSTRUCTOR_CHARACTERS));

		builder.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.STRING_ADD_STRING));
		builder.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.STRING_COMPARE));

		builder.getter(new MethodInstance(BuiltinMethodSymbol.STRING_LENGTH));
		builder.indexGet(new MethodInstance(BuiltinMethodSymbol.STRING_INDEXGET));
		builder.indexGet(new MethodInstance(BuiltinMethodSymbol.STRING_RANGEGET));

		builder.getter(new MethodInstance(BuiltinMethodSymbol.STRING_CHARACTERS));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.STRING_ISEMPTY));
		builder.method(new MethodInstance(BuiltinMethodSymbol.STRING_REMOVE_DIACRITICS));
		builder.method(new MethodInstance(BuiltinMethodSymbol.STRING_TRIM));
		builder.method(new MethodInstance(BuiltinMethodSymbol.STRING_TO_LOWER_CASE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.STRING_TO_UPPER_CASE));

		builder.operator(OperatorType.CONTAINS, new MethodInstance(BuiltinMethodSymbol.STRING_CONTAINS_CHAR));
		builder.operator(OperatorType.CONTAINS, new MethodInstance(BuiltinMethodSymbol.STRING_CONTAINS_STRING));
		return builder.build();
	}

	private BasicTypeMembers() {
	}

}
