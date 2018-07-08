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
public class TryRethrowAsExceptionExpression extends Expression {
	public final Expression value;
	
	public TryRethrowAsExceptionExpression(CodePosition position, ITypeID type, Expression value, ITypeID thrownType) {
		super(position, type, thrownType);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitTryRethrowAsException(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new TryRethrowAsExceptionExpression(position, type, tValue, thrownType);
	}
}
