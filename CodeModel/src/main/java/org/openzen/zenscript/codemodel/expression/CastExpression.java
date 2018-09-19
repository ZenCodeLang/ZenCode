/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.member.ref.CasterMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class CastExpression extends Expression {
	public final Expression target;
	public final CasterMemberRef member;
	public final boolean isImplicit;
	
	public CastExpression(CodePosition position, Expression target, CasterMemberRef member, boolean isImplicit) {
		super(position, member.toType, binaryThrow(position, target.thrownType, member.member.header.thrownType));
		
		this.target = target;
		this.member = member;
		this.isImplicit = isImplicit;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCast(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitCast(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tTarget = target.transform(transformer);
		return target == tTarget ? this : new CastExpression(position, tTarget, member, isImplicit);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new CastExpression(position, target.normalize(scope), member, isImplicit);
	}
}
