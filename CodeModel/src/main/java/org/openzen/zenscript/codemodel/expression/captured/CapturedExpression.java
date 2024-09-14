package org.openzen.zenscript.codemodel.expression.captured;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionVisitor;
import org.openzen.zenscript.codemodel.expression.ExpressionVisitorWithContext;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.type.TypeID;

public abstract class CapturedExpression extends Expression {
	public final LambdaClosure closure;

	protected CapturedExpression(CodePosition position, TypeID type, LambdaClosure closure) {
		super(position, type, null);

		this.closure = closure;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCaptured(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitCaptured(context, this);
	}

	public abstract <T> T accept(CapturedExpressionVisitor<T> visitor);
}
