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
public class DummyExpression extends Expression {
	public DummyExpression(ITypeID type) {
		super(CodePosition.BUILTIN, type, null);
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		throw new UnsupportedOperationException("This is a dummy expression");
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		throw new UnsupportedOperationException("This is a dummy expression");
	}
}
