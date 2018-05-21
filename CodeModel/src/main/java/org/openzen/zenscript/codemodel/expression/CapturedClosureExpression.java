/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class CapturedClosureExpression extends CapturedExpression {
	public final CapturedExpression value;
	
	public CapturedClosureExpression(CodePosition position, CapturedExpression value, LambdaClosure closure) {
		super(position, value.type, closure);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCapturedClosure(this);
	}

	@Override
	public <T> T accept(CapturedExpressionVisitor<T> visitor) {
		return visitor.visitRecaptured(this);
	}
}
