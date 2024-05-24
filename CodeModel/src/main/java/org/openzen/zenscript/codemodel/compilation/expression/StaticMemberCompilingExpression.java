package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;
import java.util.stream.Stream;

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

		/*Optional<Expression> asGetter = resolved.findStaticGetter(name.name)
			.map(getter -> getter.call(compiler, position, TypeID.NONE));
		if (asGetter.isPresent()) {
			return asGetter.get();
		}*/

		Optional<StaticCallable> staticGetter = resolved.findStaticGetter(name.name);
		if(staticGetter.isPresent()){
			return staticGetter.get().call(compiler, position, TypeID.NONE);
		}
		Optional<CompilableExpression> contextMember = resolved.getContextMember(name.name);
		if(contextMember.isPresent()){
			return contextMember.get().compile(compiler).eval();
		}
		return compiler.at(position).invalid(CompileErrors.noMemberInType(type, name.name));

//		return resolved.findStaticGetter(name.name)
//				.map(getter -> getter.call(compiler, position, TypeID.NONE))
//				.orElseGet(() ->
//						resolved.getContextMember(name.name)
//								.map(member -> member.compile(compiler).eval())
//								.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noMemberInType(type, name.name))));
	}

	@Override
	public CastedExpression cast(CastedEval cast) {
		return cast.of(eval());
	}

	@Override
	public Optional<CompilingCallable> call() {
		ResolvedType resolvedType = compiler.resolve(type);
		Optional<CompilingCallable> staticMethod = resolvedType.findStaticMethod(name.name)
				.map(member -> member.bindTypeArguments(compiler, name.arguments));
		if (staticMethod.isPresent())
			return staticMethod;

		return resolvedType.getContextMember(name.name).flatMap(member -> member.compile(compiler).call());
	}

	@Override
	public CompilingExpression getMember(CodePosition position, GenericName name) {
		ResolvedType resolvedType = compiler.resolve(type);
		Optional<TypeSymbol> innerType = resolvedType.findInnerType(this.name.name);
		if (innerType.isPresent())
			return new StaticMemberCompilingExpression(compiler, position, innerType.get().normalize(this.name.arguments), name);

		return new InstanceMemberCompilingExpression(compiler, position, this, name);
	}

	@Override
	public CompilingExpression assign(CompilingExpression value) {
		if (name.hasArguments())
			return compiler.invalid(position, CompileErrors.typeArgumentsNotAllowedHere());

		return compiler.resolve(type).findStaticSetter(name.name)
				.<CompilingExpression>map(setter -> new CompilingSet(compiler, position, setter, value))
				.orElseGet(() -> compiler.invalid(position, CompileErrors.noMemberInType(type, name.name)));
	}

	@Override
	public void collect(SSAVariableCollector collector) {}

	@Override
	public void linkVariables(CodeBlockStatement.VariableLinker linker) {}

	private static class CompilingSet extends AbstractCompilingExpression {
		private final StaticCallable setter;
		private final CompilingExpression value;

		public CompilingSet(ExpressionCompiler compiler, CodePosition position, StaticCallable setter, CompilingExpression value) {
			super(compiler, position);

			this.setter = setter;
			this.value = value;
		}

		@Override
		public Expression eval() {
			return setter.call(compiler, position, TypeID.NONE, value);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
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
}
