/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class CoalesceExpression extends Expression {
	public final Expression left;
	public final Expression right;
	
	public CoalesceExpression(CodePosition position, Expression left, Expression right) {
		super(position, right.type, binaryThrow(position, left.thrownType, right.thrownType));
		
		this.left = left;
		this.right = right;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCoalesce(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tLeft = left.transform(transformer);
		Expression tRight = right.transform(transformer);
		return tLeft == left && tRight == right ? this : new CoalesceExpression(position, tLeft, tRight);
	}
}
