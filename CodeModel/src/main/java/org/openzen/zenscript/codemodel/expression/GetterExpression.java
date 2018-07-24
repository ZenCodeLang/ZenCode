/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.member.ref.GetterMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class GetterExpression extends Expression {
	public final Expression target;
	public final GetterMemberRef getter;
	
	public GetterExpression(CodePosition position, Expression target, GetterMemberRef getter) {
		super(position, getter.type, target.thrownType);
		
		this.target = target;
		this.getter = getter;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGetter(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tTarget = target.transform(transformer);
		return target == tTarget ? this : new GetterExpression(position, tTarget, getter);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new GetterExpression(position, target.normalize(scope), getter);
	}
}
