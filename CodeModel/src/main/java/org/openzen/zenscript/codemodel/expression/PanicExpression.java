/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class PanicExpression extends Expression {
	public final Expression value;
	
	public PanicExpression(CodePosition position, ITypeID type, Expression value) {
		super(position, type, null);
		
		this.value = value;
	}
	
	@Override
	public boolean aborts() {
		return true;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitPanic(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitPanic(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return new PanicExpression(position, type, transformer.transform(value));
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new PanicExpression(position, type, value.normalize(scope));
	}
}
