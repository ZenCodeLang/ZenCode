package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;

public class CapturedDirectExpression extends CapturedExpression {
	public final Expression value;

	public CapturedDirectExpression(CodePosition position, LambdaClosure closure, Expression value) {
		super(position, value.type, closure);
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCapturedDirect(this);
	}

	@Override
	public <T> T accept(CapturedExpressionVisitor<T> visitor) {
		return visitor.visitCapturedDirect(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitCapturedDirect(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = transformer.transform(value);
		return tValue == value ? this : new CapturedDirectExpression(position, closure, tValue);
	}
}
