package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.compilation.expression.StaticCompilingCallable;
import org.openzen.zenscript.codemodel.compilation.impl.BoundInstanceCallable;
import org.openzen.zenscript.codemodel.compilation.impl.BoundSuperCallable;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class ParsedExpressionSuper extends ParsedExpression {
	public ParsedExpressionSuper(CodePosition position) {
		super(position);
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		Optional<LocalType> maybeLocalType = compiler.getLocalType();
		if (!maybeLocalType.isPresent())
			return compiler.invalid(position, CompileErrors.noThisInScope());

		Optional<TypeID> maybeSuperType = maybeLocalType.get().getSuperType();
		if (!maybeSuperType.isPresent())
			return compiler.invalid(position, CompileErrors.localTypeNoSuper());

		return new Compiling(compiler, position, maybeLocalType.get(), maybeSuperType.get());
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final LocalType localType;
		private final TypeID superType;

		public Compiling(ExpressionCompiler compiler, CodePosition position, LocalType localType, TypeID superType) {
			super(compiler, position);

			this.localType = localType;
			this.superType = superType;
		}

		@Override
		public CompilingExpression getMember(CodePosition position, GenericName name) {
			return new CompilingMember(compiler, position, localType.getThisType(), superType, name);
		}

		@Override
		public void collect(SSAVariableCollector collector) {

		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {

		}

		@Override
		public Expression eval() {
			return compiler.at(position).invalid(CompileErrors.superNotExpression());
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public Optional<CompilingCallable> call() {
			return localType.superCall().map(call -> new StaticCompilingCallable(compiler, call));
		}
	}

	private static class CompilingMember extends AbstractCompilingExpression {
		private final TypeID thisType;
		private final TypeID superType;
		private final GenericName name;

		public CompilingMember(ExpressionCompiler compiler, CodePosition position, TypeID thisType, TypeID superType, GenericName name) {
			super(compiler, position);

			this.thisType = thisType;
			this.superType = superType;
			this.name = name;
		}

		@Override
		public Expression eval() {
			if (name.hasArguments())
				return compiler.at(position).invalid(CompileErrors.typeArgumentsNotAllowedHere());

			return compiler.resolve(superType)
					.findGetter(name.name)
					.map(getter -> getter.call(compiler, position, compiler.at(position).getThis(thisType), TypeID.NONE))
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noGetterInType(superType, name.name)));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			if (name.hasArguments())
				return cast.invalid(CompileErrors.typeArgumentsNotAllowedHere());

			return compiler.resolve(superType)
					.findGetter(name.name)
					.map(getter -> getter.cast(compiler, position, cast, compiler.at(position).getThis(thisType), TypeID.NONE))
					.orElseGet(() -> cast.invalid(CompileErrors.noGetterInType(superType, name.name)));
		}

		@Override
		public CompilingExpression assign(CompilingExpression value) {
			if (name.hasArguments())
				return compiler.invalid(position, CompileErrors.typeArgumentsNotAllowedHere());

			return new CompilingSetMember(compiler, position, thisType, superType, name, value);
		}

		@Override
		public void collect(SSAVariableCollector collector) {

		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {

		}

		@Override
		public Optional<CompilingCallable> call() {
			return compiler.resolve(superType)
					.findMethod(name.name)
					.map(method -> new BoundSuperCallable(compiler, method, compiler.at(position).getThis(thisType), TypeID.NONE));
		}
	}

	private static class CompilingSetMember extends AbstractCompilingExpression {
		private final TypeID thisType;
		private final TypeID superType;
		private final GenericName name;
		private final CompilingExpression value;

		public CompilingSetMember(
				ExpressionCompiler compiler,
				CodePosition position,
				TypeID thisType,
				TypeID superType,
				GenericName name,
				CompilingExpression value
		) {
			super(compiler, position);

			this.thisType = thisType;
			this.superType = superType;
			this.name = name;
			this.value = value;
		}

		@Override
		public Expression eval() {
			return compiler.resolve(superType)
					.findSetter(name.name)
					.map(setter -> setter.call(compiler, position, compiler.at(position).getThis(thisType), TypeID.NONE, value))
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noSetterInType(superType, name.name)));
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
