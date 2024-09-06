package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.modifiable.ModifiableExpression;
import org.openzen.zenscript.codemodel.expression.modifiable.ModifiableInvalidExpression;
import org.openzen.zenscript.codemodel.expression.modifiable.ModifiablePropertyExpression;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class InstanceMemberCompilingExpression extends AbstractCompilingExpression {
	private final CompilingExpression instance;
	private final GenericName name;

	public InstanceMemberCompilingExpression(ExpressionCompiler compiler, CodePosition position, CompilingExpression instance, GenericName name) {
		super(compiler, position);

		this.instance = instance;
		this.name = name;
	}

	@Override
	public Expression eval() {
		if (name.hasArguments())
			return compiler.at(position).invalid(CompileErrors.typeArgumentsNotAllowedHere());

		Expression instance = this.instance.eval();
		ResolvedType resolve = compiler.resolve(instance.type);

		Optional<Expression> byGetter = resolve
				.findGetter(name.name)
				.map(getter -> getter.call(compiler, position, instance, TypeID.NONE, CompilingExpression.NONE));

		return byGetter.orElseGet(() -> resolve
				.findField(name.name)
				.map(field -> field.get(compiler.at(position), instance))
				.orElseGet(
						() -> compiler.at(position).invalid(CompileErrors.noMemberInType(instance.type, name.name))
				));

	}

	@Override
	public CastedExpression cast(CastedEval cast) {
		return cast.of(eval());
	}

	@Override
	public Optional<CompilingCallable> call() {
		Expression instance = this.instance.eval();
		ResolvedType resolvedType = compiler.resolve(instance.type);
		Optional<CompilingCallable> result = resolvedType
				.findMethod(name.name)
				.map(method -> method.bind(compiler, instance, name.arguments));
		if (!result.isPresent()) {
			result = resolvedType.findGetter(name.name)
					.flatMap(getter -> {
						Expression value = getter.call(compiler, position, instance, TypeID.NONE);
						return compiler.resolve(value.type)
								.findOperator(OperatorType.CALL)
								.map(method -> method.bind(compiler, value, name.arguments));
					});
		}
		if (!result.isPresent()) {
			result = resolvedType.findField(name.name)
					.flatMap(field -> compiler.resolve(field.getType())
							.findOperator(OperatorType.CALL)
							.map(method -> method.bind(compiler, field.get(compiler.at(position), instance), name.arguments)));
		}

		return Optional.of(result.orElseGet(() -> new InvalidCompilingExpression(compiler, position, CompileErrors.noMemberInType(instance.type, name.name))));
	}

	@Override
	public Optional<ModifiableExpression> asModifiable() {
		Expression instance = this.instance.eval();
		ResolvedType resolvedType = compiler.resolve(instance.type);
		Optional<InstanceCallable> getter = resolvedType.findGetter(name.name);
		Optional<InstanceCallable> setter = resolvedType.findSetter(name.name);
		if (getter.isPresent() && setter.isPresent()) {
			Optional<MethodInstance> getterMethod = getter.get().asSingleMethod();
			if (!getterMethod.isPresent()) {
				return Optional.of(new ModifiableInvalidExpression(position, instance.type, CompileErrors.invalidPropertyGetter(instance.type, name.name)));
			}
			Optional<MethodInstance> setterMethod = setter.get().asSingleMethod();
			if (!setterMethod.isPresent()) {
				return Optional.of(new ModifiableInvalidExpression(position, instance.type, CompileErrors.invalidPropertySetter(instance.type, name.name)));
			}
			return Optional.of(new ModifiablePropertyExpression(instance, getterMethod.get(), setterMethod.get()));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public CompilingExpression assign(CompilingExpression value) {
		return new CompilingAssignment(compiler, position, instance, name, value);
	}

	@Override
	public void collect(SSAVariableCollector collector) {
		instance.collect(collector);
	}

	@Override
	public void linkVariables(CodeBlockStatement.VariableLinker linker) {
		instance.linkVariables(linker);
	}

	private static class Setter extends AbstractCompilingExpression {
		private final Expression instance;
		private final InstanceCallable setter;
		private final CompilingExpression value;

		public Setter(
				ExpressionCompiler compiler,
				CodePosition position,
				Expression instance,
				InstanceCallable setter,
				CompilingExpression value
		) {
			super(compiler, position);

			this.instance = instance;
			this.setter = setter;
			this.value = value;
		}

		@Override
		public Expression eval() {
			return setter.call(compiler, position, instance, TypeID.NONE, value);
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

	private static class CompilingAssignment extends AbstractCompilingExpression {
		private final CompilingExpression instance;
		private final GenericName name;
		private final CompilingExpression value;

		public CompilingAssignment(ExpressionCompiler compiler, CodePosition position, CompilingExpression instance, GenericName name, CompilingExpression value) {
			super(compiler, position);

			this.instance = instance;
			this.name = name;
			this.value = value;
		}

		@Override
		public Expression eval() {
			if (name.hasArguments())
				return compiler.at(position).invalid(CompileErrors.typeArgumentsNotAllowedHere());

			Expression instance = this.instance.eval();
			ResolvedType resolvedType = compiler.resolve(instance.type);
			Optional<CompilingExpression> result = resolvedType
					.findSetter(name.name)
					.map(setter -> new Setter(compiler, position, instance, setter, value));
			if (!result.isPresent()) {
				result = resolvedType.findField(name.name)
						.map(field -> new FieldSetter(compiler, position, instance, field, value));
			}

			return result
					.map(CompilingExpression::eval)
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noSetterInType(instance.type, name.name)));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			instance.collect(collector);
			value.collect(collector);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			instance.linkVariables(linker);
			value.linkVariables(linker);
		}
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
