package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class ParsedExpressionRange extends ParsedExpression {
	private final CompilableExpression from;
	private final CompilableExpression to;

	public ParsedExpressionRange(CodePosition position, CompilableExpression from, CompilableExpression to) {
		super(position);

		this.from = from;
		this.to = to;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, from.compile(compiler), to.compile(compiler));
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression from;
		private final CompilingExpression to;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression from, CompilingExpression to) {
			super(compiler, position);

			this.from = from;
			this.to = to;
		}

		@Override
		public Expression eval() {
			Expression from = this.from.eval();
			Expression to = this.to.eval();
			return compiler.union(from.type, to.type)
					.map(t -> {
						CastedEval cast = cast(t);
						return compiler.at(position).newRange(cast.of(from).value, cast.of(to).value);
					})
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noIntersectionBetweenTypes(from.type, to.type)));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			TypeID type = cast.type.simplified();
			Optional<RangeTypeID> maybeRange = type.asRange();
			if (maybeRange.isPresent()) {
				RangeTypeID range = maybeRange.get();
				CastedExpression from = this.from.cast(cast(range.baseType));
				CastedExpression to = this.to.cast(cast(range.baseType));
				return cast.of(from.level.max(to.level), compiler.at(position).newRange(from.value, to.value));
			} else {
				ResolvedType resolvedType = compiler.resolve(type);
				return resolvedType.findImplicitConstructor()
						.map(constructor -> cast.of(constructor.call(compiler, position, TypeID.NONE, this)))
						.orElseGet(() -> cast.invalid(CompileErrors.invalidRangeType(type)));
			}
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			from.collect(collector);
			to.collect(collector);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			from.linkVariables(linker);
			to.linkVariables(linker);
		}
	}
}
