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
		MemberSet result = new MemberSet();
		result.addOperator(OperatorType.NOT, new MethodInstance(BuiltinMethodSymbol.BOOL_NOT));
		result.addOperator(OperatorType.AND, new MethodInstance(BuiltinMethodSymbol.BOOL_AND));
		result.addOperator(OperatorType.OR, new MethodInstance(BuiltinMethodSymbol.BOOL_OR));
		result.addOperator(OperatorType.XOR, new MethodInstance(BuiltinMethodSymbol.BOOL_XOR));
		result.addOperator(OperatorType.EQUALS, new MethodInstance(BuiltinMethodSymbol.BOOL_EQUALS));
		result.addOperator(OperatorType.NOTEQUALS, new MethodInstance(BuiltinMethodSymbol.BOOL_NOTEQUALS));
		result.addImplicitCast(new MethodInstance(BuiltinMethodSymbol.BOOL_TO_STRING));
		result.addStaticMethod(new MethodInstance(BuiltinMethodSymbol.BOOL_PARSE));
		return result;
	}

	private static MemberSet getByte() {
		MemberSet result = new MemberSet();
		// TODO
		return result;
	}

	private static MemberSet getSByte() {
		MemberSet result = new MemberSet();
		// TODO
		return result;
	}

	private static MemberSet getShort() {
		MemberSet result = new MemberSet();
		// TODO
		return result;
	}

	private static MemberSet getUShort() {
		MemberSet result = new MemberSet();
		// TODO
		return result;
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
