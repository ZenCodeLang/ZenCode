package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.statement.VarStatement;

public class CapturedLocalVariableExpression extends CapturedExpression {
	public final VarStatement variable;

	public CapturedLocalVariableExpression(CodePosition position, VarStatement variable, LambdaClosure closure) {
		super(position, variable.type, closure);

		this.variable = variable;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCapturedLocalVariable(this);
	}

	@Override
	public <T> T accept(CapturedExpressionVisitor<T> visitor) {
		return visitor.visitCapturedLocal(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitCapturedLocalVariable(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}
}
