package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.expression.Expression;

public class InvalidCompilingExpression extends AbstractCompilingExpression {
	private final CompileError error;

	public InvalidCompilingExpression(ExpressionCompiler compiler, CodePosition position, CompileError error) {
		super(compiler, position);
		this.error = error;
	}

	@Override
	public Expression eval() {
		return compiler.at(position).invalid(error);
	}

	@Override
	public CastedExpression cast(CastedEval cast) {
		return cast.invalid(error);
	}
}
