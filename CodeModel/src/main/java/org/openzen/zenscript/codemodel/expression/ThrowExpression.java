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
public class ThrowExpression extends Expression {
	public final Expression value;
	
	public ThrowExpression(CodePosition position, ITypeID type, Expression value) {
		super(position, type, value.type);
		
		this.value = value;
	}
	
	@Override
	public boolean aborts() {
		return true;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitThrow(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = transformer.transform(value);
		return tValue == value ? this : new ThrowExpression(position, type, value);
	}
}
