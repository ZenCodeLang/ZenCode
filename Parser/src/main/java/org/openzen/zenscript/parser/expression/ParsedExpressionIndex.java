package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Arrays;
import java.util.List;

public class ParsedExpressionIndex extends ParsedExpression {
	private final CompilableExpression value;
	private final List<CompilableExpression> indexes;

	public ParsedExpressionIndex(CodePosition position, CompilableExpression value, List<CompilableExpression> indexes) {
		super(position);

		this.value = value;
		this.indexes = indexes;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(
				compiler,
				position,
				value.compile(compiler),
				indexes.stream().map(ix -> ix.compile(compiler)).toArray(CompilingExpression[]::new));
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final Expression value;
		private final CompilingExpression[] indexes;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression value, CompilingExpression[] indexes) {
			super(compiler, position);
			this.value = value.eval();
			this.indexes = indexes;
		}

		@Override
		public Expression eval() {
			ResolvedType resolved = compiler.resolve(value.type);
			return resolved.findOperator(OperatorType.INDEXGET)
					.map(method -> method.call(compiler, position, value, TypeID.NONE, indexes))
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noOperatorInType(value.type, OperatorType.INDEXGET)));
		}

		@Override
		public CompilingExpression assign(CompilingExpression value) {
			return new CompilingSet(compiler, position, this.value, indexes, value);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			ResolvedType resolved = compiler.resolve(value.type);
			return resolved.findOperator(OperatorType.INDEXGET)
					.map(method -> method.cast(compiler, position, cast, value, TypeID.NONE, indexes))
					.orElseGet(() -> cast.invalid(CompileErrors.noOperatorInType(value.type, OperatorType.INDEXGET)));
		}
	}

	private static class CompilingSet extends AbstractCompilingExpression {
		private final Expression instance;
		private final CompilingExpression[] arguments;

		public CompilingSet(
				ExpressionCompiler compiler,
				CodePosition position,
				Expression instance,
				CompilingExpression[] indexes,
				CompilingExpression value
		) {
			super(compiler, position);

			this.instance = instance;
			this.arguments = Arrays.copyOf(indexes, indexes.length + 1);
			this.arguments[arguments.length - 1] = value;
		}

		@Override
		public Expression eval() {
			return compiler.resolve(instance.type).findOperator(OperatorType.INDEXSET)
					.map(operator -> operator.call(compiler, position, instance, TypeID.NONE, arguments))
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noOperatorInType(instance.type, OperatorType.INDEXSET)));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return compiler.resolve(instance.type).findOperator(OperatorType.INDEXSET)
					.map(operator -> operator.cast(compiler, position, cast, instance, TypeID.NONE, arguments))
					.orElseGet(() -> cast.invalid(CompileErrors.noOperatorInType(instance.type, OperatorType.INDEXSET)));
		}
	}
}
