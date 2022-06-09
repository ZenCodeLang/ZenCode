package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedExpressionCoalesce extends ParsedExpression {
	private final CompilableExpression left;
	private final CompilableExpression right;

	public ParsedExpressionCoalesce(CodePosition position, CompilableExpression left, CompilableExpression right) {
		super(position);

		this.left = left;
		this.right = right;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		CompilingExpression left = this.left.compile(compiler);
		CompilingExpression right = this.right.compile(compiler);
		return new Compiling(compiler, position, left, right);
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
			Expression left = this.left.eval();
			TypeID rightType = left.type.withoutOptional();
			Expression cRight = right.cast(cast(rightType)).value;
			return compiler.at(position).coalesce(left, cRight);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			CastedExpression cLeft = left.cast(cast(compiler.types().optionalOf(cast.type)));
			if (cLeft.isFailed())
				return cLeft;

			Expression cRight = right.cast(cast).value;
			return cast.of(compiler.at(position).coalesce(cLeft.value, cRight));
		}
	}
}
