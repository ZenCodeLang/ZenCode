package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.CastedEval;
import org.openzen.zenscript.codemodel.compilation.CastedExpression;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.expression.CompareExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class BasicTypeMembers {
	private static final MethodID CONSTRUCTOR = MethodID.staticOperator(OperatorType.CONSTRUCTOR);
	private static final MethodID COMPARE = MethodID.operator(OperatorType.COMPARE);

	public static ResolvingType get(BasicTypeID type) {
		switch (type) {
			case VOID:
			case NULL:
			case UNDETERMINED:
			case INVALID:
				return MemberSet.create(type).build();
			default:
				MemberSet.Builder builder = MemberSet.create(type);
				setup(builder, type);
				return builder.build();
		}
	}

/*	private static MemberSet getBool() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.NOT, new MethodInstance(BuiltinMethodSymbol.BOOL_NOT));
		builder.operator(OperatorType.AND, new MethodInstance(BuiltinMethodSymbol.BOOL_AND));
		builder.operator(OperatorType.OR, new MethodInstance(BuiltinMethodSymbol.BOOL_OR));
		builder.operator(OperatorType.XOR, new MethodInstance(BuiltinMethodSymbol.BOOL_XOR));
		builder.operator(OperatorType.EQUALS, new MethodInstance(BuiltinMethodSymbol.BOOL_EQUALS));
		builder.operator(OperatorType.NOTEQUALS, new MethodInstance(BuiltinMethodSymbol.BOOL_NOTEQUALS));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.BOOL_TO_STRING));
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

		builder.cast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_SBYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_SHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_USHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_INT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_UINT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_LONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_ULONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_USIZE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_FLOAT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_DOUBLE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_CHAR));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.BYTE_TO_STRING));

		builder.method(new MethodInstance(BuiltinMethodSymbol.BYTE_PARSE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.BYTE_PARSE_WITH_BASE));

		builder.field(new FieldInstance(BuiltinFieldSymbol.BYTE_MIN_VALUE));
		builder.field(new FieldInstance(BuiltinFieldSymbol.BYTE_MAX_VALUE));

		comparator(builder, BuiltinMethodSymbol.BYTE_COMPARE, BasicTypeID.BYTE);

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

		builder.cast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_BYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_SHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_USHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_INT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_UINT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_LONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_ULONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_USIZE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_FLOAT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_DOUBLE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_CHAR));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SBYTE_TO_STRING));

		builder.method(new MethodInstance(BuiltinMethodSymbol.SBYTE_PARSE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.SBYTE_PARSE_WITH_BASE));

		builder.field(new FieldInstance(BuiltinFieldSymbol.SBYTE_MIN_VALUE));
		builder.field(new FieldInstance(BuiltinFieldSymbol.SBYTE_MAX_VALUE));

		comparator(builder, BuiltinMethodSymbol.SBYTE_COMPARE, BasicTypeID.SBYTE);

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

		builder.cast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_BYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_SBYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_USHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_INT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_UINT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_LONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_ULONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_USIZE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_FLOAT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_DOUBLE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_CHAR));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_STRING));

		builder.method(new MethodInstance(BuiltinMethodSymbol.SHORT_PARSE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.SHORT_PARSE_WITH_BASE));

		builder.field(new FieldInstance(BuiltinFieldSymbol.SHORT_MIN_VALUE));
		builder.field(new FieldInstance(BuiltinFieldSymbol.SHORT_MAX_VALUE));

		comparator(builder, BuiltinMethodSymbol.SHORT_COMPARE, BasicTypeID.SHORT);

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

		builder.cast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_BYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_SBYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_SHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_INT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_UINT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_LONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_ULONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_USIZE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_FLOAT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_DOUBLE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_CHAR));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_STRING));

		builder.method(new MethodInstance(BuiltinMethodSymbol.USHORT_PARSE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.USHORT_PARSE_WITH_BASE));

		builder.field(new FieldInstance(BuiltinFieldSymbol.USHORT_MIN_VALUE));
		builder.field(new FieldInstance(BuiltinFieldSymbol.USHORT_MAX_VALUE));

		comparator(builder, BuiltinMethodSymbol.USHORT_COMPARE, BasicTypeID.USHORT);

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

		builder.cast(new MethodInstance(BuiltinMethodSymbol.INT_TO_BYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.INT_TO_SBYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.INT_TO_SHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.INT_TO_USHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.INT_TO_UINT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.INT_TO_LONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.INT_TO_ULONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.INT_TO_USIZE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.INT_TO_FLOAT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.INT_TO_DOUBLE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.INT_TO_CHAR));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.INT_TO_STRING));

		builder.method(new MethodInstance(BuiltinMethodSymbol.INT_PARSE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.INT_PARSE_WITH_BASE));

		builder.field(new FieldInstance(BuiltinFieldSymbol.INT_MIN_VALUE));
		builder.field(new FieldInstance(BuiltinFieldSymbol.INT_MAX_VALUE));

		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_COUNT_LOW_ZEROES));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_COUNT_HIGH_ZEROES));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_COUNT_LOW_ONES));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_COUNT_HIGH_ONES));

		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_HIGHEST_ONE_BIT));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_LOWEST_ONE_BIT));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_HIGHEST_ZERO_BIT));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_LOWEST_ZERO_BIT));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.INT_BIT_COUNT));

		comparator(builder, BuiltinMethodSymbol.INT_COMPARE, BasicTypeID.INT);

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

		builder.cast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_BYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_SBYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_SHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_USHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_INT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_LONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_ULONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_USIZE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_FLOAT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_DOUBLE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_CHAR));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.UINT_TO_STRING));

		builder.method(new MethodInstance(BuiltinMethodSymbol.UINT_PARSE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.UINT_PARSE_WITH_BASE));

		builder.field(new FieldInstance(BuiltinFieldSymbol.UINT_MIN_VALUE));
		builder.field(new FieldInstance(BuiltinFieldSymbol.UINT_MAX_VALUE));

		builder.getter(new MethodInstance(BuiltinMethodSymbol.UINT_COUNT_LOW_ZEROES));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.UINT_COUNT_HIGH_ZEROES));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.UINT_COUNT_LOW_ONES));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.UINT_COUNT_HIGH_ONES));

		builder.getter(new MethodInstance(BuiltinMethodSymbol.UINT_HIGHEST_ONE_BIT));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.UINT_LOWEST_ONE_BIT));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.UINT_HIGHEST_ZERO_BIT));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.UINT_LOWEST_ZERO_BIT));
		builder.getter(new MethodInstance(BuiltinMethodSymbol.UINT_BIT_COUNT));

		comparator(builder, BuiltinMethodSymbol.UINT_COMPARE, BasicTypeID.UINT);

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

		builder.cast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_BYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_SBYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_SHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_USHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_INT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_UINT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_ULONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_USIZE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_FLOAT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_DOUBLE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_CHAR));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.LONG_TO_STRING));

		builder.method(new MethodInstance(BuiltinMethodSymbol.LONG_PARSE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.LONG_PARSE_WITH_BASE));

		builder.field(new FieldInstance(BuiltinFieldSymbol.LONG_MIN_VALUE));
		builder.field(new FieldInstance(BuiltinFieldSymbol.LONG_MAX_VALUE));

		builder.getter("countLowZeroes", new MethodInstance(BuiltinMethodSymbol.LONG_COUNT_LOW_ZEROES));
		builder.getter("countHighZeroes", new MethodInstance(BuiltinMethodSymbol.LONG_COUNT_HIGH_ZEROES));
		builder.getter("countLowOnes", new MethodInstance(BuiltinMethodSymbol.LONG_COUNT_LOW_ONES));
		builder.getter("countHighOnes", new MethodInstance(BuiltinMethodSymbol.LONG_COUNT_HIGH_ONES));

		builder.getter("highestOneBit", new MethodInstance(BuiltinMethodSymbol.LONG_HIGHEST_ONE_BIT));
		builder.getter("LowestOneBit", new MethodInstance(BuiltinMethodSymbol.LONG_LOWEST_ONE_BIT));
		builder.getter("highestZeroBit", new MethodInstance(BuiltinMethodSymbol.LONG_HIGHEST_ZERO_BIT));
		builder.getter("lowestZeroBit", new MethodInstance(BuiltinMethodSymbol.LONG_LOWEST_ZERO_BIT));
		builder.getter("bitCount", new MethodInstance(BuiltinMethodSymbol.LONG_BIT_COUNT));

		comparator(builder, BuiltinMethodSymbol.LONG_COMPARE, BasicTypeID.LONG);

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

		builder.cast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_BYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_SBYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_SHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_USHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_INT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_UINT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_LONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_USIZE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_FLOAT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_DOUBLE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_CHAR));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.ULONG_TO_STRING));

		builder.method(new MethodInstance(BuiltinMethodSymbol.ULONG_PARSE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.ULONG_PARSE_WITH_BASE));

		builder.field(new FieldInstance(BuiltinFieldSymbol.ULONG_MIN_VALUE));
		builder.field(new FieldInstance(BuiltinFieldSymbol.ULONG_MAX_VALUE));

		builder.getter("countLowZeroes", new MethodInstance(BuiltinMethodSymbol.ULONG_COUNT_LOW_ZEROES));
		builder.getter("countHighZeroes", new MethodInstance(BuiltinMethodSymbol.ULONG_COUNT_HIGH_ZEROES));
		builder.getter("countLowOnes", new MethodInstance(BuiltinMethodSymbol.ULONG_COUNT_LOW_ONES));
		builder.getter("countHighOnes", new MethodInstance(BuiltinMethodSymbol.ULONG_COUNT_HIGH_ONES));

		builder.getter("highestOneBit", new MethodInstance(BuiltinMethodSymbol.ULONG_HIGHEST_ONE_BIT));
		builder.getter("LowestOneBit", new MethodInstance(BuiltinMethodSymbol.ULONG_LOWEST_ONE_BIT));
		builder.getter("highestZeroBit", new MethodInstance(BuiltinMethodSymbol.ULONG_HIGHEST_ZERO_BIT));
		builder.getter("lowestZeroBit", new MethodInstance(BuiltinMethodSymbol.ULONG_LOWEST_ZERO_BIT));
		builder.getter("bitCount", new MethodInstance(BuiltinMethodSymbol.ULONG_BIT_COUNT));

		comparator(builder, BuiltinMethodSymbol.ULONG_COMPARE, BasicTypeID.ULONG);

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

		builder.cast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_BYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_SBYTE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_SHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_USHORT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_INT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_UINT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_LONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_ULONG));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_FLOAT));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_DOUBLE));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_CHAR));
		builder.cast(new MethodInstance(BuiltinMethodSymbol.USIZE_TO_STRING));

		builder.method(new MethodInstance(BuiltinMethodSymbol.USIZE_PARSE));
		builder.method(new MethodInstance(BuiltinMethodSymbol.USIZE_PARSE_WITH_BASE));

		builder.method(new MethodInstance(BuiltinMethodSymbol.USIZE_COUNT_LOW_ZEROES));
		builder.method(new MethodInstance(BuiltinMethodSymbol.USIZE_COUNT_HIGH_ZEROES));
		builder.method(new MethodInstance(BuiltinMethodSymbol.USIZE_COUNT_LOW_ONES));
		builder.method(new MethodInstance(BuiltinMethodSymbol.USIZE_COUNT_HIGH_ONES));

		builder.method(new MethodInstance(BuiltinMethodSymbol.USIZE_HIGHEST_ONE_BIT));
		builder.method(new MethodInstance(BuiltinMethodSymbol.USIZE_LOWEST_ONE_BIT));
		builder.method(new MethodInstance(BuiltinMethodSymbol.USIZE_HIGHEST_ZERO_BIT));
		builder.method(new MethodInstance(BuiltinMethodSymbol.USIZE_LOWEST_ZERO_BIT));
		builder.method(new MethodInstance(BuiltinMethodSymbol.USIZE_BIT_COUNT));

		builder.field(new FieldInstance(BuiltinFieldSymbol.USIZE_MIN_VALUE));
		builder.field(new FieldInstance(BuiltinFieldSymbol.USIZE_MAX_VALUE));
		builder.field(new FieldInstance(BuiltinFieldSymbol.USIZE_BITS));

		comparator(builder, BuiltinMethodSymbol.USIZE_COMPARE, BasicTypeID.USIZE);

		return builder.build();
	}

	private static MemberSet getFloat() {
		MemberSet.Builder builder = MemberSet.create();

		setup(builder, BasicTypeID.FLOAT);

		builder.field(new FieldInstance(BuiltinFieldSymbol.FLOAT_MIN_VALUE));
		builder.field(new FieldInstance(BuiltinFieldSymbol.FLOAT_MAX_VALUE));

		return builder.build();
	}

	private static MemberSet getDouble() {
		MemberSet.Builder builder = MemberSet.create();

		for (BuiltinMethodSymbol method : BuiltinMethodSymbol.values()) {
			if (method.getDefiningType() == BasicTypeID.DOUBLE)
				builder.method(new MethodInstance(method));
		}

		builder.field(new FieldInstance(BuiltinFieldSymbol.DOUBLE_MIN_VALUE));
		builder.field(new FieldInstance(BuiltinFieldSymbol.DOUBLE_MAX_VALUE));

		comparator(builder, BuiltinMethodSymbol.DOUBLE_COMPARE, BasicTypeID.DOUBLE);

		return builder.build();
	}

	private static MemberSet getChar() {
		MemberSet.Builder builder = MemberSet.create();

		for (BuiltinMethodSymbol method : BuiltinMethodSymbol.values()) {
			if (method.getDefiningType() == BasicTypeID.CHAR)
				builder.method(new MethodInstance(method));
		}

		comparator(builder, BuiltinMethodSymbol.CHAR_COMPARE, BasicTypeID.CHAR);

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

		comparator(builder, BuiltinMethodSymbol.STRING_COMPARE, BasicTypeID.STRING);

		return builder.build();
	}*/

	private static void setup(MemberSet.Builder builder, BasicTypeID type) {
		for (BuiltinMethodSymbol method : BuiltinMethodSymbol.values()) {
			if (method.getDefiningType().equals(type) && method.getID().equals(COMPARE)) {
				comparator(builder);
			} else if (method.getDefiningType().equals(type) && method.getID().equals(CONSTRUCTOR)) {
				builder.constructor(new MethodInstance(method));
			}/* else if (method.getDefiningType() == type) {
				builder.method(new MethodInstance(method));
			}*/
			if (method.getDefiningType() == type) {
				builder.method(new MethodInstance(method));
				if (method.useWideningConversions) {
					for (MethodInstance widened : getWideningMethodInstances(method)) {
						builder.method(widened);
					}
				}
			}
			if (method.useWideningConversions) {
				method.getDefiningType().asType().ifPresent(definingType -> {
					BasicTypeID basicDefiningType = (BasicTypeID) definingType;
					TypeID[] wideningSources = getWideningSources(basicDefiningType);
					for (TypeID source : wideningSources) {
						if (source == type) {
							builder.method(new MethodInstance(method, method.getHeader(), basicDefiningType, true));
						}
					}
				});
			}
		}

		for (BuiltinFieldSymbol field : BuiltinFieldSymbol.values()) {
			if (field.getDefiningType() == type) {
				builder.field(new FieldInstance(field));
			}
		}
	}

	private static void comparator(MemberSet.Builder builder) {
		builder.comparator(((compiler, position, left, right, type) -> {
			Expression rightCompiled = right.eval();
			Optional<TypeID> union = compiler.union(left.type, rightCompiled.type);
			return union.map(typeID -> {
				CastedEval cast = new CastedEval(compiler, left.position, typeID, false, false);
				Expression castedLeft = cast.of(left).value;
				Expression castedRight = cast.of(rightCompiled).value;
				Expression value = new CompareExpression(
						position,
						castedLeft,
						castedRight,
						new MethodInstance(getComparator((BasicTypeID) typeID)),
						type);
				return new CastedExpression(CastedExpression.Level.EXACT, value);
			}).orElseGet(() -> new CastedExpression(CastedExpression.Level.INVALID, compiler.at(position).invalid(CompileErrors.cannotCompare(left.type, rightCompiled.type))));
		}));
	}

	private static BuiltinMethodSymbol getComparator(BasicTypeID type) {
		switch (type) {
			case BYTE: return BuiltinMethodSymbol.BYTE_COMPARE;
			case SBYTE: return BuiltinMethodSymbol.SBYTE_COMPARE;
			case SHORT: return BuiltinMethodSymbol.SHORT_COMPARE;
			case USHORT: return BuiltinMethodSymbol.USHORT_COMPARE;
			case INT: return BuiltinMethodSymbol.INT_COMPARE;
			case UINT: return BuiltinMethodSymbol.UINT_COMPARE;
			case LONG: return BuiltinMethodSymbol.LONG_COMPARE;
			case ULONG: return BuiltinMethodSymbol.ULONG_COMPARE;
			case USIZE: return BuiltinMethodSymbol.USIZE_COMPARE;
			case FLOAT: return BuiltinMethodSymbol.FLOAT_COMPARE;
			case DOUBLE: return BuiltinMethodSymbol.DOUBLE_COMPARE;
			case CHAR: return BuiltinMethodSymbol.CHAR_COMPARE;
			case STRING: return BuiltinMethodSymbol.STRING_COMPARE;
			default: throw new IllegalArgumentException("No comparator for " + type);
		}
	}

	private static MethodInstance[] getWideningMethodInstances(BuiltinMethodSymbol method) {
		FunctionHeader original = method.getHeader();
		if (original.parameters.length != 1)
			throw new IllegalArgumentException("Not doing widening on multiple method arguments");

		FunctionParameter originalParameter = original.parameters[0];
		TypeID[] wideningSources = getWideningSources((BasicTypeID) originalParameter.type);
		MethodInstance[] wideningMethodInstances = new MethodInstance[wideningSources.length];
		for (int i = 0; i < wideningSources.length; i++) {
			FunctionParameter parameter = new FunctionParameter(wideningSources[i], originalParameter.name, false);
			FunctionHeader header = new FunctionHeader(original.typeParameters, original.getReturnType(), original.thrownType, parameter);
			wideningMethodInstances[i] = new MethodInstance(method, header, method.getTargetType(), true);
		}
		return wideningMethodInstances;
	}

	private static TypeID[] getWideningSources(BasicTypeID type) {
		switch (type) {
			case BYTE:
			case SBYTE:
				return TypeID.NONE;
			case SHORT:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.SBYTE};
			case USHORT:
				return new TypeID[]{BasicTypeID.BYTE};
			case INT:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.SBYTE, BasicTypeID.SHORT, BasicTypeID.USHORT};
			case UINT:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.USHORT};
			case USIZE:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.USHORT, BasicTypeID.UINT};
			case LONG:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.SBYTE, BasicTypeID.SHORT, BasicTypeID.USHORT, BasicTypeID.INT, BasicTypeID.USIZE};
			case ULONG:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.USHORT, BasicTypeID.UINT, BasicTypeID.USIZE};
			case FLOAT:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.SBYTE, BasicTypeID.SHORT, BasicTypeID.USHORT, BasicTypeID.INT, BasicTypeID.UINT, BasicTypeID.LONG, BasicTypeID.ULONG, BasicTypeID.USIZE};
			case DOUBLE:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.SBYTE, BasicTypeID.SHORT, BasicTypeID.USHORT, BasicTypeID.INT, BasicTypeID.UINT, BasicTypeID.LONG, BasicTypeID.ULONG, BasicTypeID.USIZE, BasicTypeID.FLOAT};
			default:
				return TypeID.NONE;
		}
	}

	private BasicTypeMembers() {
	}
}
