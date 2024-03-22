package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.compilation.expression.CompilingThisExpression;
import org.openzen.zenscript.codemodel.compilation.expression.InstanceMemberCompilingExpression;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.switchvalue.ErrorSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.*;

public class ParsedExpressionVariable extends ParsedExpression {
	public final String name;
	private final List<IParsedType> typeArguments;

	public ParsedExpressionVariable(CodePosition position, String name, List<IParsedType> typeArguments) {
		super(position);

		this.name = name;
		this.typeArguments = typeArguments;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		TypeID[] typeArguments = IParsedType.compileTypes(this.typeArguments, compiler.types());

		Optional<CompilingExpression> resolved = compiler.resolve(position, new GenericName(name, typeArguments));
		if (resolved.isPresent()) {
			return new CompilingResolved(compiler, position, name, resolved.get());
		} else {
			if (compiler.getThisType().isPresent()) {
				Optional<ResolvedType.Field> field = compiler.resolve(compiler.getThisType().get()).findField(name);
				if (field.isPresent()) {
					return new InstanceMemberCompilingExpression(
							compiler,
							position,
							new CompilingThisExpression(compiler, position, compiler.getThisType().get()),
							new GenericName(name, typeArguments));
				}
			}
			return new CompilingUnresolved(compiler, position, name);
		}
	}

	@Override
	public CompilingExpression compileKey(ExpressionCompiler compiler) {
		if (typeArguments != null && !typeArguments.isEmpty())
			return compiler.invalid(position, CompileErrors.associativeKeyCannotHaveTypeParameters());

		return new CompilingKey(compiler, position, name);
	}

	private static class CompilingKey extends AbstractCompilingExpression {
		private final String name;

		public CompilingKey(ExpressionCompiler compiler, CodePosition position, String name) {
			super(compiler, position);
			this.name = name;
		}

		@Override
		public Expression eval() {
			return new ConstantStringExpression(position, name);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public void collect(SSAVariableCollector collector) {}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {}
	}

	private static class CompilingResolved extends AbstractCompilingExpression {
		private final String name;
		private final CompilingExpression resolved; // can be null

		public CompilingResolved(ExpressionCompiler compiler, CodePosition position, String name, CompilingExpression resolved) {
			super(compiler, position);
			this.name = name;
			this.resolved = resolved;
		}

		@Override
		public Expression eval() {
			return resolved.eval();
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			TypeID type = cast.type.simplified();
			return resolved.cast(cast);
		}

		@Override
		public Optional<CompilingCallable> call() {
			return resolved.call();
		}

		@Override
		public CompilingExpression assign(CompilingExpression value) {
			return resolved.assign(value);
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			resolved.collect(collector);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			resolved.linkVariables(linker);
		}

		@Override
		public CompilingExpression getMember(CodePosition position, GenericName name) {
			return resolved.getMember(position, name);
		}

		@Override
		public Optional<String> asStringKey() {
			return Optional.of(name);
		}
	}

	private static class CompilingUnresolved extends AbstractCompilingExpression implements CompilingCallable {
		private final String name;

		public CompilingUnresolved(ExpressionCompiler compiler, CodePosition position, String name) {
			super(compiler, position);
			this.name = name;
		}

		@Override
		public Expression eval() {
			return compiler.at(position).invalid(CompileErrors.noSuchVariable(compiler, name));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			TypeID type = cast.type.simplified();
			return compiler.resolve(type).getContextMember(name)
						.map(member -> member.compile(compiler).cast(cast))
						.orElseGet(() -> cast.invalid(CompileErrors.noContextMemberInType(type, name)));
		}

		@Override
		public Optional<CompilingCallable> call() {
			return Optional.of(this);
		}

		@Override
		public CompilingExpression assign(CompilingExpression value) {
			return compiler.invalid(position, CompileErrors.noSuchVariable(compiler, name));
		}

		@Override
		public void collect(SSAVariableCollector collector) {}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {}

		@Override
		public CompilingExpression getMember(CodePosition position, GenericName name) {
			return compiler.invalid(position, CompileErrors.noSuchVariable(compiler, this.name));
		}

		@Override
		public Optional<String> asStringKey() {
			return Optional.of(name);
		}

		// ###########################################
		// ###  CompilingCallable implementation   ###
		// ###########################################

		@Override
		public Expression call(CodePosition position, CompilingExpression... arguments) {
			return compiler.at(position).invalid(CompileErrors.noSuchVariable(compiler, name));
		}

		@Override
		public CastedExpression casted(CodePosition position, CastedEval cast, CompilingExpression... arguments) {
			TypeID type = cast.type.simplified();
			ResolvedType resolvedType = compiler.resolve(type);
			return resolvedType.getContextMember(name)
					.map(member -> member.compile(compiler).call()
							.map(c -> c.casted(position, cast, arguments))
							.orElseGet(() -> cast.invalid(CompileErrors.cannotCall())))
					.orElseGet(() -> cast.invalid(CompileErrors.noContextMemberInType(type, name)));
		}
	}

	@Override
	public CompilingSwitchValue compileSwitchValue(ExpressionCompiler compiler) {
		return new CompilingSwitchValue() {
			@Override
			public SwitchValue as(TypeID type) {
				ResolvedType resolved = compiler.resolve(type);
				Optional<ResolvedType.SwitchMember> switchMember = resolved.findSwitchMember(name);
				if (switchMember.isPresent()) {
					return switchMember.get().toSwitchValue(Collections.emptyList());
				} else {
					return new ErrorSwitchValue(position, CompileErrors.invalidSwitchCaseExpression());
				}
			}

			@Override
			public List<CompilingVariable> getBindings() {
				return Collections.emptyList();
			}
		};
	}

	@Override
	public Optional<CompilableLambdaHeader> asLambdaHeader() {
		return Optional.of(new CompilableLambdaHeader(BasicTypeID.UNDETERMINED, asLambdaHeaderParameter().get()));
	}

	@Override
	public Optional<CompilableLambdaHeader.Parameter> asLambdaHeaderParameter() {
		return Optional.of(new CompilableLambdaHeader.Parameter(name, BasicTypeID.UNDETERMINED));
	}
}
