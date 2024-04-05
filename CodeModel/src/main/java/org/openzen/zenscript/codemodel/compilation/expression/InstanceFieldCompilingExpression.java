package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class InstanceFieldCompilingExpression extends AbstractCompilingExpression {
	private final CompilingExpression instance;
	private final ResolvedType.Field field;

	public InstanceFieldCompilingExpression(ExpressionCompiler compiler, CodePosition position, CompilingExpression instance, ResolvedType.Field field) {
		super(compiler, position);

		this.instance = instance;
		this.field = field;
	}

	@Override
	public Expression eval() {
		Expression instance = this.instance.eval();
		return field.get(compiler.at(position), instance);
	}

	@Override
	public CastedExpression cast(CastedEval cast) {
		return cast.of(eval());
	}

	@Override
	public Optional<CompilingCallable> call() {
		Expression instance = this.instance.eval();
		ResolvedType fieldType = compiler.resolve(field.getType());
		Optional<CompilingCallable> result = fieldType
					.findOperator(OperatorType.CALL)
					.map(method -> method.bind(compiler, field.get(compiler.at(position), instance), TypeID.NONE));

		return Optional.of(result.orElseGet(() -> new InvalidCompilingExpression(compiler, position, CompileErrors.cannotCall())));
	}

	@Override
	public CompilingExpression assign(CompilingExpression value) {
		Expression instance = this.instance.eval();
		return new FieldSetter(compiler, position, instance, field, value);
	}

	@Override
	public void collect(SSAVariableCollector collector) {
		// TODO
	}

	@Override
	public void linkVariables(CodeBlockStatement.VariableLinker linker) {
		// TODO
	}

	private static class FieldSetter extends AbstractCompilingExpression {
		private final Expression instance;
		private final ResolvedType.Field field;
		private final CompilingExpression value;

		public FieldSetter(
				ExpressionCompiler compiler,
				CodePosition position,
				Expression instance,
				ResolvedType.Field field,
				CompilingExpression value
		) {
			super(compiler, position);

			this.instance = instance;
			this.field = field;
			this.value = value;
		}

		@Override
		public Expression eval() {
			return field.set(compiler.at(position), instance, value.eval());
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			// TODO
			//instance.collect(collector);
			value.collect(collector);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			value.linkVariables(linker);
		}
	}
}
