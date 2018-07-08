/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.CastExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class CasterMemberRef implements DefinitionMemberRef {
	public final CasterMember member;
	public final ITypeID toType;
	
	public CasterMemberRef(CasterMember member, ITypeID toType) {
		this.member = member;
		this.toType = toType;
	}

	@Override
	public CodePosition getPosition() {
		return member.position;
	}

	@Override
	public String describe() {
		return member.describe();
	}

	@Override
	public <T> T getTag(Class<T> type) {
		return member.getTag(type);
	}
	
	public Expression cast(CodePosition position, Expression value, boolean implicit) {
		return new CastExpression(position, value, this, implicit);
	}
	
	public boolean isImplicit() {
		return Modifiers.isImplicit(member.modifiers);
	}
}
