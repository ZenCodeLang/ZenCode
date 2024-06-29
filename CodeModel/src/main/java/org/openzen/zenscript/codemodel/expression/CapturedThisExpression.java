package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Objects;

public class CapturedThisExpression extends CapturedExpression {
	public CapturedThisExpression(CodePosition position, TypeID type, LambdaClosure closure) {
		super(position, type, closure);
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCapturedThis(this);
	}

	@Override
	public <T> T accept(CapturedExpressionVisitor<T> visitor) {
		return visitor.visitCapturedThis(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitCapturedThis(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}

		CapturedThisExpression otherCaptured = (CapturedThisExpression) obj;
		return Objects.equals(this.type, otherCaptured.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.type);
	}
}
