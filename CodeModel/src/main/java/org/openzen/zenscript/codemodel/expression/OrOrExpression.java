/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class OrOrExpression extends Expression {
	public final Expression left;
	public final Expression right;
	
	public OrOrExpression(CodePosition position, Expression left, Expression right) {
		super(position, BasicTypeID.BOOL, binaryThrow(position, left.thrownType, right.thrownType));
		
		this.left = left;
		this.right = right;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitOrOr(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tLeft = left.transform(transformer);
		Expression tRight = right.transform(transformer);
		return tLeft == left && tRight == right ? this : new OrOrExpression(position, tLeft, tRight);
	}
}
