package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zenscript.codemodel.compilation.CastedEval;
import org.openzen.zenscript.codemodel.compilation.CastedExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.expression.Expression;

public class WrappedCompilingExpression extends AbstractCompilingExpression{
	private final Expression value;

	public WrappedCompilingExpression(ExpressionCompiler compiler, Expression value) {
		super(compiler, value.position);
		this.value = value;
	}

	@Override
	public Expression eval() {
		return value;
	}

	@Override
	public CastedExpression cast(CastedEval cast) {
		return cast.of(value);
	}
}
