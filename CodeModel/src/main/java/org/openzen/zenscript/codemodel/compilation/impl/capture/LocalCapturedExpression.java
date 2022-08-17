package org.openzen.zenscript.codemodel.compilation.impl.capture;

import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.expression.CapturedClosureExpression;
import org.openzen.zenscript.codemodel.expression.CapturedExpression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;

public class LocalCapturedExpression implements LocalExpression {
	private final CapturedExpression outer;

	public LocalCapturedExpression(CapturedExpression outer) {
		this.outer = outer;
	}

	@Override
	public LocalExpression capture(LambdaClosure closure) {
		CapturedExpression result = new CapturedClosureExpression(outer.position, outer, closure);
		closure.add(result);
		return new LocalCapturedExpression(result);
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return outer.wrap(compiler);
	}
}
