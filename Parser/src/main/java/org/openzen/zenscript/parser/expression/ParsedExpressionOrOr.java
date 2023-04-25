package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

public class ParsedExpressionOrOr extends ParsedExpression {
	private final CompilableExpression left;
	private final CompilableExpression right;

	public ParsedExpressionOrOr(CodePosition position, CompilableExpression left, CompilableExpression right) {
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
			return compiler.at(position).orOr(
					left.cast(cast(BasicTypeID.BOOL)).value,
					right.cast(cast(BasicTypeID.BOOL)).value);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			left.collect(collector);
			right.collect(collector.conditional());
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			left.linkVariables(linker);
			right.linkVariables(linker);
		}
	}
}
