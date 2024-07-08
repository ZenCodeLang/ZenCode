package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionParameter;

import java.util.Objects;

public class CapturedParameterExpression extends CapturedExpression {
	public final FunctionParameter parameter;

	public CapturedParameterExpression(CodePosition position, FunctionParameter parameter, LambdaClosure closure) {
		super(position, parameter.type, closure);

		this.parameter = parameter;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCapturedParameter(this);
	}

	@Override
	public <T> T accept(CapturedExpressionVisitor<T> visitor) {
		return visitor.visitCapturedParameter(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitCapturedParameter(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CapturedParameterExpression that = (CapturedParameterExpression) o;
		return Objects.equals(parameter, that.parameter);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(parameter);
	}
}
