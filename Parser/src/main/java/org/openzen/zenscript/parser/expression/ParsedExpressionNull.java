package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;

public class ParsedExpressionNull extends ParsedExpression {
	public ParsedExpressionNull(CodePosition position) {
		super(position);
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position);
	}

	private static class Compiling extends AbstractCompilingExpression {
		public Compiling(ExpressionCompiler compiler, CodePosition position) {
			super(compiler, position);
		}

		@Override
		public Expression eval() {
			return compiler.at(position).invalid(CompileErrors.cannotInferNull());
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(compiler.at(position).constantNull(cast.type));
		}
	}
}
