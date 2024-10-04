package org.openzen.zenscript.codemodel.expression.captured;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.VariableDefinition;
import org.openzen.zenscript.codemodel.expression.*;

import java.util.Objects;

public class CapturedLocalVariableExpression extends CapturedExpression {
	public final VariableDefinition variable;

	public CapturedLocalVariableExpression(CodePosition position, VariableDefinition variable, LambdaClosure closure) {
		super(position, variable.type, closure);

		this.variable = variable;
	}

	@Override
	public <T> T accept(CapturedExpressionVisitor<T> visitor) {
		return visitor.visitCapturedLocal(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CapturedLocalVariableExpression that = (CapturedLocalVariableExpression) o;
		return Objects.equals(variable, that.variable);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(variable);
	}
}
