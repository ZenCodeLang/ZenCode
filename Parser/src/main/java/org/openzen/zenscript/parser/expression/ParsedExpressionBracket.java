package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

import java.util.List;
import java.util.Optional;

public class ParsedExpressionBracket extends ParsedExpression {
	public List<CompilableExpression> expressions;

	public ParsedExpressionBracket(CodePosition position, List<CompilableExpression> expressions) {
		super(position);

		this.expressions = expressions;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(
				compiler,
				position,
				expressions.stream().map(e -> e.compile(compiler)).toArray(CompilingExpression[]::new));
	}

	@Override
	public Optional<CompilableLambdaHeader> asLambdaHeader() {
		CompilableLambdaHeader.Parameter[] parameters = new CompilableLambdaHeader.Parameter[expressions.size()];
		for (int i = 0; i < expressions.size(); i++) {
			Optional<CompilableLambdaHeader.Parameter> parameter = expressions.get(i).asLambdaHeaderParameter();
			if (!parameter.isPresent())
				return Optional.empty();
			parameters[i] = parameter.get();
		}
		return Optional.of(new CompilableLambdaHeader(BasicTypeID.UNDETERMINED, parameters));
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression[] compiling;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression[] compiling) {
			super(compiler, position);
			this.compiling = compiling;
		}

		@Override
		public Expression eval() {
			if (compiling.length != 1) {
				return compiler.at(position).invalid(CompileErrors.bracketMultipleExpressions());
			} else {
				return compiling[0].eval();
			}
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			if (compiling.length != 1) {
				return cast.invalid(CompileErrors.bracketMultipleExpressions());
			} else {
				return compiling[0].cast(cast);
			}
		}
	}
}
