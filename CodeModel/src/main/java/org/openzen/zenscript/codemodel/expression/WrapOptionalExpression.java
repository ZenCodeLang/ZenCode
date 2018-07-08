/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class WrapOptionalExpression extends Expression {
	public final Expression value;
	
	public WrapOptionalExpression(CodePosition position, Expression value, ITypeID optionalType) {
		super(position, optionalType, value.thrownType);
		
		if (value.type.isOptional())
			throw new IllegalArgumentException("Value is already optional");
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitWrapOptional(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new WrapOptionalExpression(position, tValue, type);
	}
}
