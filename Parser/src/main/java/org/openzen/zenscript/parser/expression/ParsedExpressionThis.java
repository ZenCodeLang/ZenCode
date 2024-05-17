package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.expression.StaticCompilingCallable;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;

import java.util.Optional;

public class ParsedExpressionThis extends ParsedExpression {
	public ParsedExpressionThis(CodePosition position) {
		super(position);
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return compiler.getLocalType()
				.<CompilingExpression>map(t -> new Compiling(compiler, position, t))
				.orElseGet(() -> compiler.invalid(position, CompileErrors.noThisInScope()));
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final LocalType type;

		public Compiling(ExpressionCompiler compiler, CodePosition position, LocalType type) {
			super(compiler, position);
			this.type = type;
		}

		@Override
		public Expression eval() {
			return compiler.getThis(position).orElseThrow(() -> new AssertionError("what?")).eval();
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public Optional<CompilingCallable> call() {
			return Optional.of(new StaticCompilingCallable(compiler, type.thisCall()));
		}

		@Override
		public void collect(SSAVariableCollector collector) {

		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {

		}
	}
}
