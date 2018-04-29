/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.ConstantIntExpression;
import org.openzen.zenscript.codemodel.expression.ConstantLongExpression;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.builtin.ConstantGetterMember;
import static org.openzen.zenscript.codemodel.type.BasicTypeID.*;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import static org.openzen.zenscript.shared.CodePosition.BUILTIN;

/**
 *
 * @author Hoofdgebruiker
 */
public class BuiltinTypeMembers {
	private BuiltinTypeMembers() {}
	
	public static final ConstantGetterMember INT_GET_MIN_VALUE = new ConstantGetterMember("MIN_VALUE", position -> new ConstantIntExpression(position, Integer.MIN_VALUE));
	public static final ConstantGetterMember INT_GET_MAX_VALUE = new ConstantGetterMember("MAX_VALUE", position -> new ConstantIntExpression(position, Integer.MAX_VALUE));
	
	public static final ConstantGetterMember LONG_GET_MIN_VALUE = new ConstantGetterMember("MIN_VALUE", position -> new ConstantLongExpression(position, Long.MIN_VALUE));
	public static final ConstantGetterMember LONG_GET_MAX_VALUE = new ConstantGetterMember("MAX_VALUE", position -> new ConstantLongExpression(position, Long.MAX_VALUE));
	
	public static final OperatorMember BOOL_NOT = not(BOOL);
	
	public static final OperatorMember BYTE_NOT = not(BYTE);
	public static final OperatorMember SBYTE_NOT = not(SBYTE);
	public static final OperatorMember SHORT_NOT = not(SHORT);
	public static final OperatorMember USHORT_NOT = not(USHORT);
	public static final OperatorMember INT_NOT = not(INT);
	public static final OperatorMember UINT_NOT = not(UINT);
	public static final OperatorMember LONG_NOT = not(LONG);
	public static final OperatorMember ULONG_NOT = not(ULONG);
	
	public static final OperatorMember BYTE_NEG = neg(BYTE);
	public static final OperatorMember SBYTE_NEG = neg(SBYTE);
	public static final OperatorMember SHORT_NEG = neg(SHORT);
	public static final OperatorMember USHORT_NEG = neg(USHORT);
	public static final OperatorMember INT_NEG = neg(INT);
	public static final OperatorMember UINT_NEG = neg(UINT);
	public static final OperatorMember LONG_NEG = neg(LONG);
	public static final OperatorMember ULONG_NEG = neg(ULONG);
	public static final OperatorMember FLOAT_NEG = neg(FLOAT);
	public static final OperatorMember DOUBLE_NEG = neg(DOUBLE);
	
	public static final OperatorMember BYTE_ADD_BYTE = add(BYTE, BYTE);
	public static final OperatorMember SBYTE_ADD_SBYTE = add(SBYTE, SBYTE);
	public static final OperatorMember SHORT_ADD_SHORT = add(SHORT, SHORT);
	public static final OperatorMember USHORT_ADD_USHORT = add(USHORT, USHORT);
	public static final OperatorMember INT_ADD_INT = add(INT, INT);
	public static final OperatorMember UINT_ADD_UINT = add(UINT, UINT);
	public static final OperatorMember LONG_ADD_LONG = add(LONG, LONG);
	public static final OperatorMember ULONG_ADD_ULONG = add(ULONG, ULONG);
	public static final OperatorMember FLOAT_ADD_FLOAT = add(FLOAT, FLOAT);
	public static final OperatorMember DOUBLE_ADD_DOUBLE = add(DOUBLE, DOUBLE);
	public static final OperatorMember STRING_ADD_STRING = add(STRING, STRING);
	
	public static final OperatorMember BYTE_SUB_BYTE = sub(BYTE, BYTE);
	public static final OperatorMember SBYTE_SUB_SBYTE = sub(SBYTE, SBYTE);
	public static final OperatorMember SHORT_SUB_SHORT = sub(SHORT, SHORT);
	public static final OperatorMember USHORT_SUB_USHORT = sub(USHORT, USHORT);
	public static final OperatorMember INT_SUB_INT = sub(INT, INT);
	public static final OperatorMember UINT_SUB_UINT = sub(UINT, UINT);
	public static final OperatorMember LONG_SUB_LONG = sub(LONG, LONG);
	public static final OperatorMember ULONG_SUB_ULONG = sub(ULONG, ULONG);
	public static final OperatorMember FLOAT_SUB_FLOAT = sub(FLOAT, FLOAT);
	public static final OperatorMember DOUBLE_SUB_DOUBLE = sub(DOUBLE, DOUBLE);
	
	public static final OperatorMember BYTE_MUL_BYTE = mul(BYTE, BYTE);
	public static final OperatorMember SBYTE_MUL_SBYTE = mul(SBYTE, SBYTE);
	public static final OperatorMember SHORT_MUL_SHORT = mul(SHORT, SHORT);
	public static final OperatorMember USHORT_MUL_USHORT = mul(USHORT, USHORT);
	public static final OperatorMember INT_MUL_INT = mul(INT, INT);
	public static final OperatorMember UINT_MUL_UINT = mul(UINT, UINT);
	public static final OperatorMember LONG_MUL_LONG = mul(LONG, LONG);
	public static final OperatorMember ULONG_MUL_ULONG = mul(ULONG, ULONG);
	public static final OperatorMember FLOAT_MUL_FLOAT = mul(FLOAT, FLOAT);
	public static final OperatorMember DOUBLE_MUL_DOUBLE = mul(DOUBLE, DOUBLE);
	
	public static final OperatorMember BYTE_DIV_BYTE = div(BYTE, BYTE);
	public static final OperatorMember SBYTE_DIV_SBYTE = div(SBYTE, SBYTE);
	public static final OperatorMember SHORT_DIV_SHORT = div(SHORT, SHORT);
	public static final OperatorMember USHORT_DIV_USHORT = div(USHORT, USHORT);
	public static final OperatorMember INT_DIV_INT = div(INT, INT);
	public static final OperatorMember UINT_DIV_UINT = div(UINT, UINT);
	public static final OperatorMember LONG_DIV_LONG = div(LONG, LONG);
	public static final OperatorMember ULONG_DIV_ULONG = div(ULONG, ULONG);
	public static final OperatorMember FLOAT_DIV_FLOAT = div(FLOAT, FLOAT);
	public static final OperatorMember DOUBLE_DIV_DOUBLE = div(DOUBLE, DOUBLE);
	
	public static final OperatorMember BYTE_MOD_BYTE = mod(BYTE, BYTE);
	public static final OperatorMember SBYTE_MOD_SBYTE = mod(SBYTE, SBYTE);
	public static final OperatorMember SHORT_MOD_SHORT = mod(SHORT, SHORT);
	public static final OperatorMember USHORT_MOD_USHORT = mod(USHORT, USHORT);
	public static final OperatorMember INT_MOD_INT = mod(INT, INT);
	public static final OperatorMember UINT_MOD_UINT = mod(UINT, UINT);
	public static final OperatorMember LONG_MOD_LONG = mod(LONG, LONG);
	public static final OperatorMember ULONG_MOD_ULONG = mod(ULONG, ULONG);
	
	public static final CasterMember INT_TO_BYTE = castExplicit(BYTE);
	public static final CasterMember INT_TO_SBYTE = castExplicit(SBYTE);
	public static final CasterMember INT_TO_SHORT = castExplicit(SHORT);
	public static final CasterMember INT_TO_USHORT = castExplicit(USHORT);
	public static final CasterMember INT_TO_UINT = castImplicit(UINT);
	public static final CasterMember INT_TO_LONG = castImplicit(LONG);
	public static final CasterMember INT_TO_ULONG = castImplicit(ULONG);
	public static final CasterMember INT_TO_FLOAT = castImplicit(FLOAT);
	public static final CasterMember INT_TO_DOUBLE = castImplicit(DOUBLE);
	public static final CasterMember INT_TO_CHAR = castExplicit(CHAR);
	public static final CasterMember INT_TO_STRING = castImplicit(STRING);
	
	public static final CasterMember LONG_TO_BYTE = castExplicit(BYTE);
	public static final CasterMember LONG_TO_SBYTE = castExplicit(SBYTE);
	public static final CasterMember LONG_TO_SHORT = castExplicit(SHORT);
	public static final CasterMember LONG_TO_USHORT = castExplicit(USHORT);
	public static final CasterMember LONG_TO_INT = castExplicit(INT);
	public static final CasterMember LONG_TO_UINT = castExplicit(UINT);
	public static final CasterMember LONG_TO_ULONG = castImplicit(ULONG);
	public static final CasterMember LONG_TO_FLOAT = castImplicit(FLOAT);
	public static final CasterMember LONG_TO_DOUBLE = castImplicit(DOUBLE);
	public static final CasterMember LONG_TO_CHAR = castExplicit(CHAR);
	public static final CasterMember LONG_TO_STRING = castImplicit(STRING);
	
	public static final GetterMember FLOAT_BITS = new GetterMember(BUILTIN, 0, "bits", UINT);
	public static final GetterMember DOUBLE_BITS = new GetterMember(BUILTIN, 0, "bits", ULONG);
	public static final MethodMember FLOAT_FROMBITS = new MethodMember(BUILTIN, Modifiers.STATIC, "fromBits", new FunctionHeader(FLOAT, new FunctionParameter(UINT)));
	public static final MethodMember DOUBLE_FROMBITS = new MethodMember(BUILTIN, Modifiers.STATIC, "fromBits", new FunctionHeader(DOUBLE, new FunctionParameter(ULONG)));
	
	private static OperatorMember not(ITypeID result) {
		return new OperatorMember(BUILTIN, 0, OperatorType.NOT, new FunctionHeader(result));
	}
	
	private static OperatorMember neg(ITypeID result) {
		return new OperatorMember(BUILTIN, 0, OperatorType.NEG, new FunctionHeader(result));
	}
	
	private static OperatorMember add(ITypeID operand, ITypeID result) {
		return new OperatorMember(BUILTIN, 0, OperatorType.ADD, new FunctionHeader(result, new FunctionParameter(operand)));
	}
	
	private static OperatorMember sub(ITypeID operand, ITypeID result) {
		return new OperatorMember(BUILTIN, 0, OperatorType.SUB, new FunctionHeader(result, new FunctionParameter(operand)));
	}
	
	private static OperatorMember mul(ITypeID operand, ITypeID result) {
		return new OperatorMember(BUILTIN, 0, OperatorType.MUL, new FunctionHeader(result, new FunctionParameter(operand)));
	}
	
	private static OperatorMember div(ITypeID operand, ITypeID result) {
		return new OperatorMember(BUILTIN, 0, OperatorType.DIV, new FunctionHeader(result, new FunctionParameter(operand)));
	}
	
	private static OperatorMember mod(ITypeID operand, ITypeID result) {
		return new OperatorMember(BUILTIN, 0, OperatorType.MOD, new FunctionHeader(result, new FunctionParameter(operand)));
	}
	
	private static CasterMember castExplicit(ITypeID result) {
		return new CasterMember(CodePosition.BUILTIN, 0, result);
	}
	
	private static CasterMember castImplicit(ITypeID result) {
		return new CasterMember(CodePosition.BUILTIN, Modifiers.IMPLICIT, result);
	}
}
