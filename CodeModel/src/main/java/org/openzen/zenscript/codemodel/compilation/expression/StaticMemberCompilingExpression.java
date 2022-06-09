package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class StaticMemberCompilingExpression extends AbstractCompilingExpression {
	private final TypeID type;
	private final GenericName name;

	public StaticMemberCompilingExpression(ExpressionCompiler compiler, CodePosition position, TypeID type, GenericName name) {
		super(compiler, position);

		this.type = type;
		this.name = name;
	}

	@Override
	public Expression eval() {
		if (name.hasArguments())
			return compiler.at(position).invalid(CompileErrors.typeArgumentsNotAllowedHere());

		ResolvedType resolved = compiler.resolve(type);
		return resolved.findStaticGetter(name.name)
				.map(getter -> getter.apply(compiler.at(position)))
				.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noMemberInType(type, name.name)));
	}

	@Override
	public CastedExpression cast(CastedEval cast) {
		return cast.of(eval());
	}

	@Override
	public Optional<StaticCallable> call() {
		return compiler.resolve(type).findStaticMethod(name.name)
				.map(member -> member.bindTypeArguments(name.arguments));
	}

	@Override
	public CompilingExpression assign(CompilingExpression value) {
		if (name.hasArguments())
			return compiler.invalid(position, CompileErrors.typeArgumentsNotAllowedHere());

		return compiler.resolve(type).findStaticSetter(name.name)
				.<CompilingExpression>map(setter -> new CompilingSet(compiler, position, setter, value))
				.orElseGet(() -> compiler.invalid(position, CompileErrors.noMemberInType(type, name.name)));
	}

	private static class CompilingSet extends AbstractCompilingExpression {
		private final ResolvedType.StaticSetter setter;
		private final CompilingExpression value;

		public CompilingSet(ExpressionCompiler compiler, CodePosition position, ResolvedType.StaticSetter setter, CompilingExpression value) {
			super(compiler, position);

			this.setter = setter;
			this.value = value;
		}

		@Override
		public Expression eval() {
			return setter.apply(compiler.at(position), value.cast(cast(setter.getType())).value);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}
	}
}
