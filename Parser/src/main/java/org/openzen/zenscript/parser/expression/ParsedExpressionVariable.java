package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.switchvalue.ErrorSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
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
		return new Compiling(compiler, position, name, resolved.orElse(null));
	}

	@Override
	public CompilingExpression compileKey(ExpressionCompiler compiler) {
		if (!typeArguments.isEmpty())
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
	}

	private static class Compiling extends AbstractCompilingExpression implements StaticCallable {
		private final String name;
		private final CompilingExpression resolved; // can be null

		public Compiling(ExpressionCompiler compiler, CodePosition position, String name, CompilingExpression resolved) {
			super(compiler, position);
			this.name = name;
			this.resolved = resolved;
		}

		@Override
		public Expression eval() {
			if (resolved != null) {
				return resolved.eval();
			} else {
				return compiler.at(position).invalid(CompileErrors.noSuchVariable(compiler, name));
			}
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			TypeID type = cast.type.simplified();
			if (resolved != null) {
				return resolved.cast(cast);
			} else {
				return compiler.resolve(type).getContextMember(name)
						.map(member -> member.cast(cast))
						.orElseGet(() -> cast.invalid(CompileErrors.noContextMemberInType(type, name)));
			}
		}

		@Override
		public Optional<StaticCallable> call() {
			if (resolved == null) {
				return Optional.of(this);
			} else {
				return resolved.call();
			}
		}

		@Override
		public CompilingExpression assign(CompilingExpression value) {
			if (resolved != null) {
				return resolved.assign(value);
			} else {
				return compiler.invalid(position, CompileErrors.noSuchVariable(compiler, name));
			}
		}

		@Override
		public CompilingExpression getMember(CodePosition position, GenericName name) {
			if (resolved != null) {
				return resolved.getMember(position, name);
			} else {
				return compiler.invalid(position, CompileErrors.noSuchVariable(compiler, this.name));
			}
		}

		@Override
		public Optional<String> asStringKey() {
			return Optional.of(name);
		}

		// ###########################################
		// ###   ResolvedCallable implementation   ###
		// ### (only used on unresolved variables) ###
		// ###########################################

		@Override
		public Expression call(ExpressionBuilder builder, CompilingExpression... arguments) {
			return compiler.at(position).invalid(CompileErrors.noSuchVariable(compiler, name));
		}

		@Override
		public CastedExpression casted(ExpressionBuilder builder, CastedEval cast, CompilingExpression... arguments) {
			TypeID type = cast.type.simplified();
			ResolvedType resolvedType = compiler.resolve(type);
			return resolvedType.getContextMember(name)
					.map(member -> member.call()
							.map(c -> c.casted(compiler.at(position), cast, arguments))
							.orElseGet(() -> cast.invalid(CompileErrors.cannotCall())))
					.orElseGet(() -> cast.invalid(CompileErrors.noContextMemberInType(type, name)));
		}

		@Override
		public Optional<FunctionHeader> getSingleHeader() {
			return Optional.empty();
		}
	}

	@Override
	public SwitchValue asSwitchValue(TypeID type, ExpressionCompiler compiler) {
		ResolvedType resolved = compiler.resolve(type);
		Optional<ResolvedType.SwitchMember> switchMember = resolved.findSwitchMember(name);
		if (switchMember.isPresent()) {
			return switchMember.get().toSwitchValue(new String[0]);
		} else {
			return new ErrorSwitchValue(position, CompileErrors.invalidSwitchCaseExpression());
		}
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
