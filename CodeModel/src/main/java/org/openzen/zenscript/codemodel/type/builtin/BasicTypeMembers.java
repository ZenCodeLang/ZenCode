package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

public class BasicTypeMembers {
	private static final MemberSet NO_MEMBERS = new MemberSet();

	public static MemberSet get(BasicTypeID type) {
		switch (type) {
			case VOID: return NO_MEMBERS;
			case NULL: return NO_MEMBERS;
			case BOOL: return getBool();
			case BYTE: return getByte();
			case SBYTE: return getSByte();
			case SHORT: return getShort();
			case USHORT: return getUShort();
			case INT: return getInt();
			case UINT: return getUInt();
			case LONG: return getLong();
			case ULONG: return getULong();
			case USIZE: return getUSize();
			case FLOAT: return getFloat();
			case DOUBLE: return getDouble();
			case CHAR: return getChar();
			case STRING: return getString();
			case UNDETERMINED: return NO_MEMBERS;
			case INVALID: return NO_MEMBERS;
			default: throw new IllegalArgumentException();
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
		builder.staticMethod("parse", new MethodInstance(BuiltinMethodSymbol.BOOL_PARSE));
		return builder.build();
	}

	private static MemberSet getByte() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.NOT, new MethodInstance(BuiltinMethodSymbol.BYTE_NOT));
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

		builder.method("parse",new MethodInstance(BuiltinMethodSymbol.BYTE_PARSE));
		builder.method("parse",new MethodInstance(BuiltinMethodSymbol.BYTE_PARSE_WITH_BASE));

		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.BYTE_GET_MIN_VALUE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.BYTE_GET_MAX_VALUE));

		return builder.build();
	}

	private static MemberSet getSByte() {
		MemberSet.Builder builder = MemberSet.create();
		builder.operator(OperatorType.NOT, new MethodInstance(BuiltinMethodSymbol.SBYTE_NOT));
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

		builder.method("parse",new MethodInstance(BuiltinMethodSymbol.SBYTE_PARSE));
		builder.method("parse",new MethodInstance(BuiltinMethodSymbol.SBYTE_PARSE_WITH_BASE));

		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.SBYTE_GET_MIN_VALUE));
		builder.staticGetter(new MethodInstance(BuiltinMethodSymbol.SBYTE_GET_MAX_VALUE));
		return builder.build();
	}

	private static MemberSet getShort() {
		MemberSet.Builder result = MemberSet.create();
		result.operator(OperatorType.NOT, new MethodInstance(BuiltinMethodSymbol.SHORT_NOT));
		result.operator(OperatorType.INCREMENT, new MethodInstance(BuiltinMethodSymbol.SHORT_INC));
		result.operator(OperatorType.DECREMENT, new MethodInstance(BuiltinMethodSymbol.SHORT_DEC));
		result.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.SHORT_ADD_SHORT));
		result.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.SHORT_SUB_SHORT));
		result.operator(OperatorType.MUL, new MethodInstance(BuiltinMethodSymbol.SHORT_MUL_SHORT));
		result.operator(OperatorType.DIV, new MethodInstance(BuiltinMethodSymbol.SHORT_DIV_SHORT));
		result.operator(OperatorType.MOD, new MethodInstance(BuiltinMethodSymbol.SHORT_MOD_SHORT));
		result.operator(OperatorType.AND, new MethodInstance(BuiltinMethodSymbol.SHORT_AND_SHORT));
		result.operator(OperatorType.OR, new MethodInstance(BuiltinMethodSymbol.SHORT_OR_SHORT));
		result.operator(OperatorType.XOR, new MethodInstance(BuiltinMethodSymbol.SHORT_XOR_SHORT));
		result.operator(OperatorType.SHL, new MethodInstance(BuiltinMethodSymbol.SHORT_SHL));
		result.operator(OperatorType.SHR, new MethodInstance(BuiltinMethodSymbol.SHORT_SHR));
		result.operator(OperatorType.USHR, new MethodInstance(BuiltinMethodSymbol.SHORT_USHR));
		result.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.SHORT_COMPARE));

		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_BYTE));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_SBYTE));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_USHORT));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_INT));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_UINT));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_LONG));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_ULONG));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_USIZE));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_FLOAT));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_DOUBLE));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_CHAR));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.SHORT_TO_STRING));

		result.method("parse",new MethodInstance(BuiltinMethodSymbol.SHORT_PARSE));
		result.method("parse",new MethodInstance(BuiltinMethodSymbol.SHORT_PARSE_WITH_BASE));

		result.staticGetter(new MethodInstance(BuiltinMethodSymbol.SHORT_GET_MIN_VALUE));
		result.staticGetter(new MethodInstance(BuiltinMethodSymbol.SHORT_GET_MAX_VALUE));
		return result.build();
	}

	private static MemberSet getUShort() {
		MemberSet.Builder result = MemberSet.create();
		result.operator(OperatorType.NOT, new MethodInstance(BuiltinMethodSymbol.USHORT_NOT));
		result.operator(OperatorType.INCREMENT, new MethodInstance(BuiltinMethodSymbol.USHORT_INC));
		result.operator(OperatorType.DECREMENT, new MethodInstance(BuiltinMethodSymbol.USHORT_DEC));
		result.operator(OperatorType.ADD, new MethodInstance(BuiltinMethodSymbol.USHORT_ADD_USHORT));
		result.operator(OperatorType.SUB, new MethodInstance(BuiltinMethodSymbol.USHORT_SUB_USHORT));
		result.operator(OperatorType.MUL, new MethodInstance(BuiltinMethodSymbol.USHORT_MUL_USHORT));
		result.operator(OperatorType.DIV, new MethodInstance(BuiltinMethodSymbol.USHORT_DIV_USHORT));
		result.operator(OperatorType.MOD, new MethodInstance(BuiltinMethodSymbol.USHORT_MOD_USHORT));
		result.operator(OperatorType.AND, new MethodInstance(BuiltinMethodSymbol.USHORT_AND_USHORT));
		result.operator(OperatorType.OR, new MethodInstance(BuiltinMethodSymbol.USHORT_OR_USHORT));
		result.operator(OperatorType.XOR, new MethodInstance(BuiltinMethodSymbol.USHORT_XOR_USHORT));
		result.operator(OperatorType.SHL, new MethodInstance(BuiltinMethodSymbol.USHORT_SHL));
		result.operator(OperatorType.SHR, new MethodInstance(BuiltinMethodSymbol.USHORT_SHR));
		result.operator(OperatorType.COMPARE, new MethodInstance(BuiltinMethodSymbol.USHORT_COMPARE));

		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_BYTE));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_SBYTE));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_SHORT));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_INT));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_UINT));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_LONG));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_ULONG));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_USIZE));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_FLOAT));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_DOUBLE));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_CHAR));
		result.implicitCast(new MethodInstance(BuiltinMethodSymbol.USHORT_TO_STRING));

		result.method("parse",new MethodInstance(BuiltinMethodSymbol.USHORT_PARSE));
		result.method("parse",new MethodInstance(BuiltinMethodSymbol.USHORT_PARSE_WITH_BASE));

		result.staticGetter(new MethodInstance(BuiltinMethodSymbol.USHORT_GET_MIN_VALUE));
		result.staticGetter(new MethodInstance(BuiltinMethodSymbol.USHORT_GET_MAX_VALUE));
		return result.build();
	}

	private static MemberSet getInt() {
		MemberSet result = new MemberSet();
		// TODO
		return result;
	}

	private static MemberSet getUInt() {
		MemberSet result = new MemberSet();
		// TODO
		return result;
	}

	private static MemberSet getLong() {
		MemberSet result = new MemberSet();
		// TODO
		return result;
	}

	private static MemberSet getULong() {
		MemberSet result = new MemberSet();
		// TODO
		return result;
	}

	private static MemberSet getUSize() {
		MemberSet result = new MemberSet();
		// TODO
		return result;
	}

	private static MemberSet getFloat() {
		MemberSet result = new MemberSet();
		// TODO
		return result;
	}

	private static MemberSet getDouble() {
		MemberSet result = new MemberSet();
		// TODO
		return result;
	}

	private static MemberSet getChar() {
		MemberSet result = new MemberSet();
		// TODO
		return result;
	}

	private static MemberSet getString() {
		MemberSet result = new MemberSet();
		// TODO
		return result;
	}

	private BasicTypeMembers() {}
}
