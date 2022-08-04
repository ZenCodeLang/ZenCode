package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class InstanceMemberCompilingExpression extends AbstractCompilingExpression {
	private final Expression instance;
	private final GenericName name;

	public InstanceMemberCompilingExpression(ExpressionCompiler compiler, CodePosition position, Expression instance, GenericName name) {
		super(compiler, position);

		this.instance = instance;
		this.name = name;
	}

	@Override
	public Expression eval() {
		if (name.hasArguments())
			return compiler.at(position).invalid(CompileErrors.typeArgumentsNotAllowedHere());

		return compiler.resolve(instance.type)
				.findGetter(name.name)
				.map(getter -> getter.call(compiler, position, instance, TypeID.NONE, CompilingExpression.NONE))
				.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noMemberInType(instance.type, name.name)));
	}

	@Override
	public CastedExpression cast(CastedEval cast) {
		return cast.of(eval());
	}

	@Override
	public Optional<CompilingCallable> call() {
		return compiler.resolve(instance.type)
				.findMethod(name.name)
				.map(method -> method.bind(instance, name.arguments));
	}

	@Override
	public CompilingExpression assign(CompilingExpression value) {
		if (name.hasArguments())
			return compiler.invalid(position, CompileErrors.typeArgumentsNotAllowedHere());

		return compiler.resolve(instance.type)
				.findSetter(name.name)
				.<CompilingExpression>map(setter -> new Setter(compiler, position, instance, setter, value))
				.orElseGet(() -> compiler.invalid(position, CompileErrors.noSetterInType(instance.type, name.name)));
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
	}
}
