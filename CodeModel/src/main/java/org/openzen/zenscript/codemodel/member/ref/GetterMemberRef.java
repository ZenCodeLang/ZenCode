/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetterExpression;
import org.openzen.zenscript.codemodel.expression.StaticGetterExpression;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class GetterMemberRef implements DefinitionMemberRef {
	public final GetterMember member;
	public final ITypeID type;
	
	public GetterMemberRef(GetterMember member, ITypeID type) {
		this.member = member;
		this.type = type;
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
	
	public boolean isStatic() {
		return member.isStatic();
	}
	
	public Expression get(CodePosition position, Expression target) {
		return new GetterExpression(position, target, this);
	}
	
	public Expression getStatic(CodePosition position) {
		return new StaticGetterExpression(position, this);
	}
}
