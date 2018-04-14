/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class GetFunctionParameterExpression extends Expression {
	public final FunctionParameter parameter;
	
	public GetFunctionParameterExpression(CodePosition position, FunctionParameter parameter) {
		super(position, parameter.type);
		
		this.parameter = parameter;
	}
	
	@Override
	public Expression assign(CodePosition position, TypeScope scope, Expression value) {
		return new SetFunctionParameterExpression(position, parameter, value.castImplicit(position, scope, type));
	}
	
	@Override
	public CapturedExpression capture(CodePosition position, LambdaClosure closure) {
		CapturedExpression result = new CapturedParameterExpression(position, parameter, closure);
		closure.add(result);
		return result;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGetFunctionParameter(this);
	}
}
