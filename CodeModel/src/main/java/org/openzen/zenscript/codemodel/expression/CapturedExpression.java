package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.TypeID;

public abstract class CapturedExpression extends Expression {
	public final LambdaClosure closure;

	public CapturedExpression(CodePosition position, TypeID type, LambdaClosure closure) {
		super(position, type, null);

		this.closure = closure;
	}

	@Override
	public CapturedExpression capture(CodePosition position, LambdaClosure closure) {
		CapturedExpression result = new CapturedClosureExpression(position, this, closure);
		closure.add(result);
		return result;
	}

	public abstract <T> T accept(CapturedExpressionVisitor<T> visitor);
}
