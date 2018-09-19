/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;

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

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitCapturedClosure(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = transformer.transform(value);
		if (!(tValue instanceof CapturedExpression)) {
			throw new IllegalStateException("Transformed CapturedExpression must also be a CapturedExpression!");
		} else {
			return tValue == value ? this : new CapturedClosureExpression(position, (CapturedExpression)tValue, closure);
		}
	}

	@Override
	public CapturedExpression normalize(TypeScope scope) {
		return new CapturedClosureExpression(position, value.normalize(scope), closure);
	}
}
