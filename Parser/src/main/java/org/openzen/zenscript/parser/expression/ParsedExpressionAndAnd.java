package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.BinaryExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

public class ParsedExpressionAndAnd extends ParsedExpression {
	private final CompilableExpression left;
	private final CompilableExpression right;

	public ParsedExpressionAndAnd(
			CodePosition position,
			CompilableExpression left,
			CompilableExpression right) {
		super(position);

		this.left = left;
		this.right = right;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, left.compile(compiler), right.compile(compiler));
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression left;
		private final CompilingExpression right;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression left, CompilingExpression right) {
			super(compiler, position);
			this.left = left;
			this.right = right;
		}

		@Override
		public Expression eval() {
			return compiler.at(position).binary(BinaryExpression.Operator.AND_AND,
					left.cast(cast(BasicTypeID.BOOL)).value,
					right.cast(cast(BasicTypeID.BOOL)).value);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}
	}
}
