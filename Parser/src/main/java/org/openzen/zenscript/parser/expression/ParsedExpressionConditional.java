package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

public class ParsedExpressionConditional extends ParsedExpression {
	private final CompilableExpression condition;
	private final CompilableExpression ifThen;
	private final CompilableExpression ifElse;

	public ParsedExpressionConditional(CodePosition position, CompilableExpression condition, CompilableExpression ifThen, CompilableExpression ifElse) {
		super(position);

		this.condition = condition;
		this.ifThen = ifThen;
		this.ifElse = ifElse;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		CompilingExpression condition = this.condition.compile(compiler);
		CompilingExpression ifThen = this.ifThen.compile(compiler);
		CompilingExpression ifElse = this.ifElse.compile(compiler);
		return new Compiling(compiler, position, condition, ifThen, ifElse);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression condition;
		private final CompilingExpression ifThen;
		private final CompilingExpression ifElse;

		public Compiling(
				ExpressionCompiler compiler,
				CodePosition position,
				CompilingExpression condition,
				CompilingExpression ifThen,
				CompilingExpression ifElse
		) {
			super(compiler, position);
			this.condition = condition;
			this.ifThen = ifThen;
			this.ifElse = ifElse;
		}

		@Override
		public Expression eval() {
			Expression condition = this.condition.cast(cast(BasicTypeID.BOOL)).value;
			Expression ifThen = this.ifThen.eval();
			Expression ifElse = this.ifElse.eval();
			return compiler.union(ifThen.type, ifElse.type)
					.map(t -> {
						CastedEval cast = cast(t);
						return compiler.at(position).ternary(condition, cast.of(ifThen).value, cast.of(ifElse).value);
					})
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noIntersectionBetweenTypes(ifThen.type, ifElse.type)));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			Expression condition = this.condition.cast(cast(BasicTypeID.BOOL)).value;
			CastedExpression ifThen = this.ifThen.cast(cast);
			CastedExpression ifElse = this.ifElse.cast(cast);
			CastedExpression.Level level = ifThen.level.max(ifElse.level);
			return cast.of(level, compiler.at(position).ternary(condition, ifThen.value, ifElse.value));
		}
	}
}
