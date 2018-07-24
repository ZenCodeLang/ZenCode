/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 * Converts a value from X? to X. Throws a NullPointerException if the value is null.
 * 
 * @author Hoofdgebruiker
 */
public class CheckNullExpression extends Expression {
	public final Expression value;
	
	public CheckNullExpression(CodePosition position, Expression value) {
		super(position, value.type.unwrap(), value.thrownType);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCheckNull(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = transformer.transform(value);
		return value == tValue ? this : new CheckNullExpression(position, tValue);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new CheckNullExpression(position, value.normalize(scope));
	}
}
