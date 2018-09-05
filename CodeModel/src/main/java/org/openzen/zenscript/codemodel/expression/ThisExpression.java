/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ThisExpression extends Expression {
	public ThisExpression(CodePosition position, ITypeID type) {
		super(position, type, null);
	}
	
	@Override
	public IPartialExpression capture(CodePosition position, LambdaClosure closure) {
		CapturedExpression result = new CapturedThisExpression(position, type, closure);
		closure.add(result);
		return result;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitThis(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitThis(context, this);
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
