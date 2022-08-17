package org.openzen.zenscript.codemodel.compilation.impl.capture;

import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;

public interface LocalExpression {
	LocalExpression capture(LambdaClosure closure);

	CompilingExpression compile(ExpressionCompiler compiler);
}
