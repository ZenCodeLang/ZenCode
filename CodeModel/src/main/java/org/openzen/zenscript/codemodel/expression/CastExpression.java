/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class CastExpression extends Expression {
	public final Expression target;
	public final CasterMember member;
	public final boolean isImplicit;
	
	public CastExpression(CodePosition position, Expression target, CasterMember member, boolean isImplicit) {
		super(position, member.getTargetType());
		
		this.target = target;
		this.member = member;
		this.isImplicit = isImplicit;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCast(this);
	}
}
