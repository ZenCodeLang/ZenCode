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
public class CapturedDirectExpression extends CapturedExpression {
	public final Expression value;
	
	public CapturedDirectExpression(CodePosition position, LambdaClosure closure, Expression value) {
		super(position, value.type, closure);
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCapturedDirect(this);
	}

	@Override
	public <T> T accept(CapturedExpressionVisitor<T> visitor) {
		return visitor.visitCapturedDirect(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = transformer.transform(value);
		return tValue == value ? this : new CapturedDirectExpression(position, closure, tValue);
	}

	@Override
	public CapturedExpression normalize(TypeScope scope) {
		return new CapturedDirectExpression(position, closure, value.normalize(scope));
	}
}
