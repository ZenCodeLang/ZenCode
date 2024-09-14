package org.openzen.zenscript.codemodel.expression.captured;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;

import java.util.Objects;

public class CapturedClosureExpression extends CapturedExpression {
	public final CapturedExpression value;

	public CapturedClosureExpression(CodePosition position, CapturedExpression value, LambdaClosure closure) {
		super(position, value.type, closure);

		this.value = value;
	}

	@Override
	public <T> T accept(CapturedExpressionVisitor<T> visitor) {
		return visitor.visitRecaptured(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = transformer.transform(value);
		if (!(tValue instanceof CapturedExpression)) {
			throw new IllegalStateException("Transformed CapturedExpression must also be a CapturedExpression!");
		} else {
			return tValue == value ? this : new CapturedClosureExpression(position, (CapturedExpression) tValue, closure);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CapturedClosureExpression that = (CapturedClosureExpression) o;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}
}
