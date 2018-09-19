/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class GetLocalVariableExpression extends Expression {
	public final VarStatement variable;
	
	public GetLocalVariableExpression(CodePosition position, VarStatement variable) {
		super(position, variable.type, null);
		
		this.variable = variable;
	}
	
	@Override
	public Expression assign(CodePosition position, TypeScope scope, Expression value) {
		return new SetLocalVariableExpression(position, variable, value.castImplicit(position, scope, type));
	}
	
	@Override
	public CapturedExpression capture(CodePosition position, LambdaClosure closure) {
		CapturedExpression result = new CapturedLocalVariableExpression(position, variable, closure);
		closure.add(result);
		return result;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGetLocalVariable(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitGetLocalVariable(context, this);
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
