/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstantIntExpression extends Expression {
	public final int value;
	
	public ConstantIntExpression(CodePosition position, int value) {
		super(position, BasicTypeID.INT, null);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConstantInt(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitConstantInt(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return this;
	}
}
