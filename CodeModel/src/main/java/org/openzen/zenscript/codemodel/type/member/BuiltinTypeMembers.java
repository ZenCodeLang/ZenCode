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
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.builtin.ConstantGetterMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class BuiltinTypeMembers {
	public static final ConstantGetterMember INT_GET_MIN_VALUE = new ConstantGetterMember("MIN_VALUE", position -> new ConstantIntExpression(position, Integer.MIN_VALUE));
	public static final ConstantGetterMember INT_GET_MAX_VALUE = new ConstantGetterMember("MAX_VALUE", position -> new ConstantIntExpression(position, Integer.MAX_VALUE));
	
	public static final OperatorMember INT_ADD_INT = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.ADD, new FunctionHeader(BasicTypeID.INT, new FunctionParameter(BasicTypeID.INT)));
	public static final OperatorMember INT_ADD_LONG = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.ADD, new FunctionHeader(BasicTypeID.LONG, new FunctionParameter(BasicTypeID.LONG)));
	public static final OperatorMember INT_ADD_FLOAT = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.ADD, new FunctionHeader(BasicTypeID.FLOAT, new FunctionParameter(BasicTypeID.FLOAT)));
	public static final OperatorMember INT_ADD_DOUBLE = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.ADD, new FunctionHeader(BasicTypeID.DOUBLE, new FunctionParameter(BasicTypeID.DOUBLE)));
	
	public static final OperatorMember INT_SUB_INT = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.SUB, new FunctionHeader(BasicTypeID.INT, new FunctionParameter(BasicTypeID.INT)));
	public static final OperatorMember INT_SUB_LONG = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.SUB, new FunctionHeader(BasicTypeID.LONG, new FunctionParameter(BasicTypeID.LONG)));
	public static final OperatorMember INT_SUB_FLOAT = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.SUB, new FunctionHeader(BasicTypeID.FLOAT, new FunctionParameter(BasicTypeID.FLOAT)));
	public static final OperatorMember INT_SUB_DOUBLE = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.SUB, new FunctionHeader(BasicTypeID.DOUBLE, new FunctionParameter(BasicTypeID.DOUBLE)));
	
	public static final OperatorMember INT_MUL_INT = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.MUL, new FunctionHeader(BasicTypeID.INT, new FunctionParameter(BasicTypeID.INT)));
	public static final OperatorMember INT_MUL_LONG = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.MUL, new FunctionHeader(BasicTypeID.LONG, new FunctionParameter(BasicTypeID.LONG)));
	public static final OperatorMember INT_MUL_FLOAT = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.MUL, new FunctionHeader(BasicTypeID.FLOAT, new FunctionParameter(BasicTypeID.FLOAT)));
	public static final OperatorMember INT_MUL_DOUBLE = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.MUL, new FunctionHeader(BasicTypeID.DOUBLE, new FunctionParameter(BasicTypeID.DOUBLE)));
	
	public static final OperatorMember INT_DIV_INT = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.DIV, new FunctionHeader(BasicTypeID.INT, new FunctionParameter(BasicTypeID.INT)));
	public static final OperatorMember INT_DIV_LONG = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.DIV, new FunctionHeader(BasicTypeID.LONG, new FunctionParameter(BasicTypeID.LONG)));
	public static final OperatorMember INT_DIV_FLOAT = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.DIV, new FunctionHeader(BasicTypeID.FLOAT, new FunctionParameter(BasicTypeID.FLOAT)));
	public static final OperatorMember INT_DIV_DOUBLE = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.DIV, new FunctionHeader(BasicTypeID.DOUBLE, new FunctionParameter(BasicTypeID.DOUBLE)));
	
	public static final OperatorMember INT_MOD_INT = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.MOD, new FunctionHeader(BasicTypeID.INT, new FunctionParameter(BasicTypeID.INT)));
	public static final OperatorMember INT_MOD_LONG = new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.MOD, new FunctionHeader(BasicTypeID.LONG, new FunctionParameter(BasicTypeID.LONG)));
	
	public static final CasterMember INT_TO_BYTE = new CasterMember(CodePosition.BUILTIN, 0, BasicTypeID.BYTE);
	public static final CasterMember INT_TO_SBYTE = new CasterMember(CodePosition.BUILTIN, 0, BasicTypeID.SBYTE);
	public static final CasterMember INT_TO_SHORT = new CasterMember(CodePosition.BUILTIN, 0, BasicTypeID.SHORT);
	public static final CasterMember INT_TO_USHORT = new CasterMember(CodePosition.BUILTIN, 0, BasicTypeID.USHORT);
	public static final CasterMember INT_TO_UINT = new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.UINT);
	public static final CasterMember INT_TO_LONG = new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.LONG);
	public static final CasterMember INT_TO_ULONG = new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.ULONG);
	public static final CasterMember INT_TO_FLOAT = new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.FLOAT);
	public static final CasterMember INT_TO_DOUBLE = new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.DOUBLE);
	public static final CasterMember INT_TO_CHAR = new CasterMember(CodePosition.BUILTIN, 0, BasicTypeID.CHAR);
	public static final CasterMember INT_TO_STRING = new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.STRING);
}
