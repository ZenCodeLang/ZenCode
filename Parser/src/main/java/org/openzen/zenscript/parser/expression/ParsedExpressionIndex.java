package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
		CompilingExpression value = this.value.compile(compiler);
		ExpressionCompiler indexCompiler = compiler.withDollar(value);
		return new Compiling(
				compiler,
				position,
				value,
				indexes.stream().map(ix -> ix.compile(indexCompiler)).toArray(CompilingExpression[]::new));
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression value;
		private final CompilingExpression[] indexes;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression value, CompilingExpression[] indexes) {
			super(compiler, position);
			this.value = value;
			this.indexes = indexes;
		}

		@Override
		public Expression eval() {
			Expression value = this.value.eval();
			if (value.type == BasicTypeID.INVALID)
				return value;

			ResolvedType resolved = compiler.resolve(value.type);
			return resolved.findOperator(OperatorType.INDEXGET)
					.map(method -> method.call(compiler, position, value, TypeID.NONE, indexes))
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noOperatorInType(value.type, OperatorType.INDEXGET)));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			Expression value = this.value.eval();
			if (value.type == BasicTypeID.INVALID)
				return CastedExpression.invalid(value);

			ResolvedType resolved = compiler.resolve(value.type);
			return resolved.findOperator(OperatorType.INDEXGET)
					.map(method -> method.cast(compiler, position, cast, value, TypeID.NONE, indexes))
					.orElseGet(() -> cast.invalid(CompileErrors.noOperatorInType(value.type, OperatorType.INDEXGET)));
		}

		@Override
		public CompilingExpression assign(CompilingExpression assignedValue) {
			return new CompilingSet(compiler, position, value, indexes, assignedValue);
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			value.collect(collector);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			value.linkVariables(linker);
		}
	}

	private static class CompilingSet extends AbstractCompilingExpression {
		private final CompilingExpression instance;
		private final CompilingExpression[] arguments;

		public CompilingSet(
				ExpressionCompiler compiler,
				CodePosition position,
				CompilingExpression instance,
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
			Expression instance = this.instance.eval();
			return compiler.resolve(instance.type).findOperator(OperatorType.INDEXSET)
					.map(operator -> operator.call(compiler, position, instance, TypeID.NONE, arguments))
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noOperatorInType(instance.type, OperatorType.INDEXSET)));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			Expression instance = this.instance.eval();
			if (instance.type == BasicTypeID.INVALID)
				return CastedExpression.invalid(instance);

			return compiler.resolve(instance.type).findOperator(OperatorType.INDEXSET)
					.map(operator -> operator.cast(compiler, position, cast, instance, TypeID.NONE, arguments))
					.orElseGet(() -> cast.invalid(CompileErrors.noOperatorInType(instance.type, OperatorType.INDEXSET)));
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			instance.collect(collector);
			for (CompilingExpression argument : arguments) {
				argument.collect(collector);
			}
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			instance.linkVariables(linker);
			for (CompilingExpression argument : arguments)
				argument.linkVariables(linker);
		}
	}

	private static class DollarExpression extends AbstractCompilingExpression {
		private final InstanceCallable getter;
		private final Expression array;

		public DollarExpression(ExpressionCompiler compiler, CodePosition position, InstanceCallable getter, Expression array) {
			super(compiler, position);

			this.getter = getter;
			this.array = array;
		}

		@Override
		public Expression eval() {
			return getter.call(compiler, position, array, TypeID.NONE, CompilingExpression.NONE);
		}

		@Override
		public void collect(SSAVariableCollector collector) {

		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {

		}
	}
}
