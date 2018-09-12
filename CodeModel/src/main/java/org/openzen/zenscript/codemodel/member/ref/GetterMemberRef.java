/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetterExpression;
import org.openzen.zenscript.codemodel.expression.StaticGetterExpression;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class GetterMemberRef extends PropertyRef {
	public final GetterMember member;
	
	public GetterMemberRef(ITypeID owner, GetterMember member, GenericMapper mapper) {
		super(owner, member, mapper);
		
		this.member = member;
	}
	
	public Expression get(CodePosition position, Expression target) {
		return new GetterExpression(position, target, this);
	}
	
	public Expression getStatic(CodePosition position) {
		return new StaticGetterExpression(position, this);
	}

	@Override
	public GetterMemberRef getOverrides() {
		return member.getOverrides();
	}
}
