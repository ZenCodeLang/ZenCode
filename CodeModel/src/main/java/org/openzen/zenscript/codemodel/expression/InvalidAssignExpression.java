/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class InvalidAssignExpression extends Expression {
	public final InvalidExpression target;
	public final Expression source;
	
	public InvalidAssignExpression(CodePosition position, InvalidExpression target, Expression source) {
		super(position, source.type, source.thrownType);
		
		this.target = target;
		this.source = source;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitInvalidAssign(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitInvalidAssign(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return new InvalidAssignExpression(
				position,
				target,
				transformer.transform(source));
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new InvalidAssignExpression(position, target, source.normalize(scope));
	}
}
