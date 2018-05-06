/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ArrayExpression extends Expression {
	public final Expression[] expressions;
	public final ArrayTypeID arrayType;
	
	public ArrayExpression(CodePosition position, Expression[] expressions, ArrayTypeID type) {
		super(position, type);
		
		this.expressions = expressions;
		this.arrayType = type;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitArray(this);
	}
}
