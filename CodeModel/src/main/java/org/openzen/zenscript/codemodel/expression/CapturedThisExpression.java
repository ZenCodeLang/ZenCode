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
public class CapturedThisExpression extends CapturedExpression {
	public CapturedThisExpression(CodePosition position, ITypeID type, LambdaClosure closure) {
		super(position, type, closure);
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCapturedThis(this);
	}

	@Override
	public <T> T accept(CapturedExpressionVisitor<T> visitor) {
		return visitor.visitCapturedThis(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}
}
