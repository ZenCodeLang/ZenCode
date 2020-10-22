/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ArrayExpression extends Expression {
	public final Expression[] expressions;
	public final ArrayTypeID arrayType;
	
	public ArrayExpression(CodePosition position, Expression[] expressions, TypeID type) {
		super(position, type, multiThrow(position, expressions));
		
		this.expressions = expressions;
		this.arrayType = (ArrayTypeID)type;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitArray(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitArray(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression[] tExpressions = Expression.transform(expressions, transformer);
		return tExpressions == expressions ? this : new ArrayExpression(position, tExpressions, type);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		Expression[] normalized = new Expression[expressions.length];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = expressions[i].normalize(scope);
		return new ArrayExpression(position, normalized, type.getNormalized());
	}
}
