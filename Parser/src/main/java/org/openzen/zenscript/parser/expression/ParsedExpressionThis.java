package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.ResolvedCallable;
import org.openzen.zenscript.compiler.expression.*;

import java.util.Optional;

public class ParsedExpressionThis extends ParsedExpression {
	public ParsedExpressionThis(CodePosition position) {
		super(position);
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return compiler.getThisType()
				.<CompilingExpression>map(t -> new Compiling(compiler, position, t))
				.orElseGet(() -> new InvalidCompilingExpression(
						compiler,
						position,
						CompileExceptionCode.USING_THIS_OUTSIDE_TYPE,
						"Not in an instance method; cannot use this"));
	}

	private static class Compiling extends AbstractCompilingExpression implements ResolvedCallable {
		private final TypeID type;

		public Compiling(ExpressionCompiler compiler, CodePosition position, TypeID type) {
			super(compiler, position);
			this.type = type;
		}

		@Override
		public Expression as(TypeID type) {
			return compiler.at(position, type).getThis(this.type);
		}

		@Override
		public TypeMatch matches(TypeID returnType) {
			return compiler.matchType(type, returnType);
		}

		@Override
		public Optional<ResolvedCallable> call() {
			return Optional.of(this);
		}

		@Override
		public InferredType inferType() {
			return InferredType.success(type);
		}

		// #######################################
		// ### ResolvedCallable implementation ###
		// #######################################

		@Override
		public Expression call(TypeID returnType, CompilingExpression... arguments) {
			return compiler.resolve(type).getConstructor().call(arguments);
		}

		@Override
		public TypeMatch matches(TypeID returnType, CompilingExpression... arguments) {
			return TypeMatch.NONE;
		}

		@Override
		public InferredType inferReturnType(CompilingExpression... arguments) {
			return InferredType.success(BasicTypeID.VOID);
		}
	}
}
