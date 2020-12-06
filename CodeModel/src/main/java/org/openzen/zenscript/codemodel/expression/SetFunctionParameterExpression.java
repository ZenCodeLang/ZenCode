package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.scope.TypeScope;

public class SetFunctionParameterExpression extends Expression {
	public final FunctionParameter parameter;
	public final Expression value;

	public SetFunctionParameterExpression(CodePosition position, FunctionParameter parameter, Expression value) {
		super(position, parameter.type, value.thrownType);

		this.parameter = parameter;
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSetFunctionParameter(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitSetFunctionParameter(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new SetFunctionParameterExpression(position, parameter, tValue);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new SetFunctionParameterExpression(position, parameter, value.normalize(scope));
	}
}
