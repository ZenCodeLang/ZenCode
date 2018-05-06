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
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
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
	
	public static final ClassDefinition T_BOOL = new ClassDefinition(BUILTIN, null, "not", Modifiers.EXPORT);
	public static final ClassDefinition T_BYTE = new ClassDefinition(BUILTIN, null, "not", Modifiers.EXPORT);
	public static final ClassDefinition T_SBYTE = new ClassDefinition(BUILTIN, null, "not", Modifiers.EXPORT);
	public static final ClassDefinition T_SHORT = new ClassDefinition(BUILTIN, null, "not", Modifiers.EXPORT);
	public static final ClassDefinition T_USHORT = new ClassDefinition(BUILTIN, null, "not", Modifiers.EXPORT);
	public static final ClassDefinition T_INT = new ClassDefinition(BUILTIN, null, "not", Modifiers.EXPORT);
	public static final ClassDefinition T_UINT = new ClassDefinition(BUILTIN, null, "not", Modifiers.EXPORT);
	public static final ClassDefinition T_LONG = new ClassDefinition(BUILTIN, null, "not", Modifiers.EXPORT);
	public static final ClassDefinition T_ULONG = new ClassDefinition(BUILTIN, null, "not", Modifiers.EXPORT);
	public static final ClassDefinition T_FLOAT = new ClassDefinition(BUILTIN, null, "not", Modifiers.EXPORT);
	public static final ClassDefinition T_DOUBLE = new ClassDefinition(BUILTIN, null, "not", Modifiers.EXPORT);
	public static final ClassDefinition T_CHAR = new ClassDefinition(BUILTIN, null, "not", Modifiers.EXPORT);
	public static final ClassDefinition T_STRING = new ClassDefinition(BUILTIN, null, "not", Modifiers.EXPORT);
	
	public static final OperatorMember BOOL_NOT = not(T_BOOL, BOOL);
	
	public static final OperatorMember BYTE_NOT = not(T_BYTE, BYTE);
	public static final OperatorMember SBYTE_NOT = not(T_SBYTE, SBYTE);
	public static final OperatorMember SHORT_NOT = not(T_SHORT, SHORT);
	public static final OperatorMember USHORT_NOT = not(T_USHORT, USHORT);
	public static final OperatorMember INT_NOT = not(T_INT, INT);
	public static final OperatorMember UINT_NOT = not(T_UINT, UINT);
	public static final OperatorMember LONG_NOT = not(T_LONG, LONG);
	public static final OperatorMember ULONG_NOT = not(T_ULONG, ULONG);
	
	public static final OperatorMember SBYTE_NEG = neg(T_SBYTE, SBYTE);
	public static final OperatorMember SHORT_NEG = neg(T_SHORT, SHORT);
	public static final OperatorMember INT_NEG = neg(T_INT, INT);
	public static final OperatorMember LONG_NEG = neg(T_LONG, LONG);
	public static final OperatorMember FLOAT_NEG = neg(T_FLOAT, FLOAT);
	public static final OperatorMember DOUBLE_NEG = neg(T_DOUBLE, DOUBLE);
	
	public static final OperatorMember BYTE_ADD_BYTE = add(T_BYTE, BYTE, BYTE);
	public static final OperatorMember SBYTE_ADD_SBYTE = add(T_SBYTE, SBYTE, SBYTE);
	public static final OperatorMember SHORT_ADD_SHORT = add(T_SHORT, SHORT, SHORT);
	public static final OperatorMember USHORT_ADD_USHORT = add(T_USHORT, USHORT, USHORT);
	public static final OperatorMember INT_ADD_INT = add(T_INT, INT, INT);
	public static final OperatorMember UINT_ADD_UINT = add(T_UINT, UINT, UINT);
	public static final OperatorMember LONG_ADD_LONG = add(T_LONG, LONG, LONG);
	public static final OperatorMember ULONG_ADD_ULONG = add(T_ULONG, ULONG, ULONG);
	public static final OperatorMember FLOAT_ADD_FLOAT = add(T_FLOAT, FLOAT, FLOAT);
	public static final OperatorMember DOUBLE_ADD_DOUBLE = add(T_DOUBLE, DOUBLE, DOUBLE);
	public static final OperatorMember STRING_ADD_STRING = add(T_STRING, STRING, STRING);
	
	public static final OperatorMember BYTE_SUB_BYTE = sub(T_BYTE, BYTE, BYTE);
	public static final OperatorMember SBYTE_SUB_SBYTE = sub(T_SBYTE, SBYTE, SBYTE);
	public static final OperatorMember SHORT_SUB_SHORT = sub(T_SHORT, SHORT, SHORT);
	public static final OperatorMember USHORT_SUB_USHORT = sub(T_USHORT, USHORT, USHORT);
	public static final OperatorMember INT_SUB_INT = sub(T_INT, INT, INT);
	public static final OperatorMember UINT_SUB_UINT = sub(T_UINT, UINT, UINT);
	public static final OperatorMember LONG_SUB_LONG = sub(T_LONG, LONG, LONG);
	public static final OperatorMember ULONG_SUB_ULONG = sub(T_ULONG, ULONG, ULONG);
	public static final OperatorMember FLOAT_SUB_FLOAT = sub(T_FLOAT, FLOAT, FLOAT);
	public static final OperatorMember DOUBLE_SUB_DOUBLE = sub(T_DOUBLE, DOUBLE, DOUBLE);
	
	public static final OperatorMember BYTE_MUL_BYTE = mul(T_BYTE, BYTE, BYTE);
	public static final OperatorMember SBYTE_MUL_SBYTE = mul(T_SBYTE, SBYTE, SBYTE);
	public static final OperatorMember SHORT_MUL_SHORT = mul(T_SHORT, SHORT, SHORT);
	public static final OperatorMember USHORT_MUL_USHORT = mul(T_USHORT, USHORT, USHORT);
	public static final OperatorMember INT_MUL_INT = mul(T_INT, INT, INT);
	public static final OperatorMember UINT_MUL_UINT = mul(T_UINT, UINT, UINT);
	public static final OperatorMember LONG_MUL_LONG = mul(T_LONG, LONG, LONG);
	public static final OperatorMember ULONG_MUL_ULONG = mul(T_ULONG, ULONG, ULONG);
	public static final OperatorMember FLOAT_MUL_FLOAT = mul(T_FLOAT, FLOAT, FLOAT);
	public static final OperatorMember DOUBLE_MUL_DOUBLE = mul(T_DOUBLE, DOUBLE, DOUBLE);
	
	public static final OperatorMember BYTE_DIV_BYTE = div(T_BYTE, BYTE, BYTE);
	public static final OperatorMember SBYTE_DIV_SBYTE = div(T_SBYTE, SBYTE, SBYTE);
	public static final OperatorMember SHORT_DIV_SHORT = div(T_SHORT, SHORT, SHORT);
	public static final OperatorMember USHORT_DIV_USHORT = div(T_USHORT, USHORT, USHORT);
	public static final OperatorMember INT_DIV_INT = div(T_INT, INT, INT);
	public static final OperatorMember UINT_DIV_UINT = div(T_UINT, UINT, UINT);
	public static final OperatorMember LONG_DIV_LONG = div(T_LONG, LONG, LONG);
	public static final OperatorMember ULONG_DIV_ULONG = div(T_ULONG, ULONG, ULONG);
	public static final OperatorMember FLOAT_DIV_FLOAT = div(T_FLOAT, FLOAT, FLOAT);
	public static final OperatorMember DOUBLE_DIV_DOUBLE = div(T_DOUBLE, DOUBLE, DOUBLE);
	
	public static final OperatorMember BYTE_MOD_BYTE = mod(T_BYTE, BYTE, BYTE);
	public static final OperatorMember SBYTE_MOD_SBYTE = mod(T_SBYTE, SBYTE, SBYTE);
	public static final OperatorMember SHORT_MOD_SHORT = mod(T_SHORT, SHORT, SHORT);
	public static final OperatorMember USHORT_MOD_USHORT = mod(T_USHORT, USHORT, USHORT);
	public static final OperatorMember INT_MOD_INT = mod(T_INT, INT, INT);
	public static final OperatorMember UINT_MOD_UINT = mod(T_UINT, UINT, UINT);
	public static final OperatorMember LONG_MOD_LONG = mod(T_LONG, LONG, LONG);
	public static final OperatorMember ULONG_MOD_ULONG = mod(T_ULONG, ULONG, ULONG);
	
	public static final CasterMember INT_TO_BYTE = castExplicit(T_BYTE, BYTE);
	public static final CasterMember INT_TO_SBYTE = castExplicit(T_SBYTE, SBYTE);
	public static final CasterMember INT_TO_SHORT = castExplicit(T_SHORT, SHORT);
	public static final CasterMember INT_TO_USHORT = castExplicit(T_USHORT, USHORT);
	public static final CasterMember INT_TO_UINT = castImplicit(T_UINT, UINT);
	public static final CasterMember INT_TO_LONG = castImplicit(T_LONG, LONG);
	public static final CasterMember INT_TO_ULONG = castImplicit(T_ULONG, ULONG);
	public static final CasterMember INT_TO_FLOAT = castImplicit(T_FLOAT, FLOAT);
	public static final CasterMember INT_TO_DOUBLE = castImplicit(T_DOUBLE, DOUBLE);
	public static final CasterMember INT_TO_CHAR = castExplicit(T_CHAR, CHAR);
	public static final CasterMember INT_TO_STRING = castImplicit(T_STRING, STRING);
	
	public static final CasterMember LONG_TO_BYTE = castExplicit(T_BYTE, BYTE);
	public static final CasterMember LONG_TO_SBYTE = castExplicit(T_SBYTE, SBYTE);
	public static final CasterMember LONG_TO_SHORT = castExplicit(T_SHORT, SHORT);
	public static final CasterMember LONG_TO_USHORT = castExplicit(T_USHORT, USHORT);
	public static final CasterMember LONG_TO_INT = castExplicit(T_INT, INT);
	public static final CasterMember LONG_TO_UINT = castExplicit(T_UINT, UINT);
	public static final CasterMember LONG_TO_ULONG = castImplicit(T_ULONG, ULONG);
	public static final CasterMember LONG_TO_FLOAT = castImplicit(T_FLOAT, FLOAT);
	public static final CasterMember LONG_TO_DOUBLE = castImplicit(T_DOUBLE, DOUBLE);
	public static final CasterMember LONG_TO_CHAR = castExplicit(T_CHAR, CHAR);
	public static final CasterMember LONG_TO_STRING = castImplicit(T_STRING, STRING);
	
	public static final GetterMember FLOAT_BITS = new GetterMember(BUILTIN, T_FLOAT, 0, "bits", UINT);
	public static final GetterMember DOUBLE_BITS = new GetterMember(BUILTIN, T_DOUBLE, 0, "bits", ULONG);
	public static final MethodMember FLOAT_FROMBITS = new MethodMember(BUILTIN, T_FLOAT, Modifiers.STATIC, "fromBits", new FunctionHeader(FLOAT, new FunctionParameter(UINT)));
	public static final MethodMember DOUBLE_FROMBITS = new MethodMember(BUILTIN, T_FLOAT, Modifiers.STATIC, "fromBits", new FunctionHeader(DOUBLE, new FunctionParameter(ULONG)));
	
	private static OperatorMember not(ClassDefinition cls, ITypeID result) {
		return new OperatorMember(BUILTIN, cls, 0, OperatorType.NOT, new FunctionHeader(result));
	}
	
	private static OperatorMember neg(ClassDefinition cls, ITypeID result) {
		return new OperatorMember(BUILTIN, cls, 0, OperatorType.NEG, new FunctionHeader(result));
	}
	
	private static OperatorMember add(ClassDefinition cls, ITypeID operand, ITypeID result) {
		return new OperatorMember(BUILTIN, cls, 0, OperatorType.ADD, new FunctionHeader(result, new FunctionParameter(operand)));
	}
	
	private static OperatorMember sub(ClassDefinition cls, ITypeID operand, ITypeID result) {
		return new OperatorMember(BUILTIN, cls, 0, OperatorType.SUB, new FunctionHeader(result, new FunctionParameter(operand)));
	}
	
	private static OperatorMember mul(ClassDefinition cls, ITypeID operand, ITypeID result) {
		return new OperatorMember(BUILTIN, cls, 0, OperatorType.MUL, new FunctionHeader(result, new FunctionParameter(operand)));
	}
	
	private static OperatorMember div(ClassDefinition cls, ITypeID operand, ITypeID result) {
		return new OperatorMember(BUILTIN, cls, 0, OperatorType.DIV, new FunctionHeader(result, new FunctionParameter(operand)));
	}
	
	private static OperatorMember mod(ClassDefinition cls, ITypeID operand, ITypeID result) {
		return new OperatorMember(BUILTIN, cls, 0, OperatorType.MOD, new FunctionHeader(result, new FunctionParameter(operand)));
	}
	
	private static CasterMember castExplicit(ClassDefinition cls, ITypeID result) {
		return new CasterMember(CodePosition.BUILTIN, cls, 0, result);
	}
	
	private static CasterMember castImplicit(ClassDefinition cls, ITypeID result) {
		return new CasterMember(CodePosition.BUILTIN, cls, Modifiers.IMPLICIT, result);
	}
}
