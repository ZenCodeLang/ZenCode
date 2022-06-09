package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;

public class ParsedExpressionBool extends ParsedExpression {
	private final boolean value;

	public ParsedExpressionBool(CodePosition position, boolean value) {
		super(position);

		this.value = value;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, value);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final boolean value;

		public Compiling(ExpressionCompiler compiler, CodePosition position, boolean value) {
			super(compiler, position);
			this.value = value;
		}

		@Override
		public Expression eval() {
			return compiler.at(position).constant(value);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

	}
}
