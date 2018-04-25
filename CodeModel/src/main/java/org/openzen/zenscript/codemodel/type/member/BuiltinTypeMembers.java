/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.ConstantIntExpression;
import org.openzen.zenscript.codemodel.member.CasterMember;
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
