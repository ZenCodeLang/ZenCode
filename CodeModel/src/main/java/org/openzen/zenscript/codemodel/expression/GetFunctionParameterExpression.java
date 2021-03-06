package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.scope.TypeScope;

public class GetFunctionParameterExpression extends Expression {
	public final FunctionParameter parameter;

	public GetFunctionParameterExpression(CodePosition position, FunctionParameter parameter) {
		super(position, parameter.type, null);

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

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitGetFunctionParameter(context, this);
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
