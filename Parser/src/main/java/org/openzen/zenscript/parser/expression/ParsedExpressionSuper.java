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

public class ParsedExpressionSuper extends ParsedExpression {
	public ParsedExpressionSuper(CodePosition position) {
		super(position);
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return compiler.getThisType()
				.flatMap(type -> compiler.resolve(type).getSuperType())
				.<CompilingExpression>map(superType -> new Compiling(compiler, position, superType))
				.orElseGet(() -> new InvalidCompilingExpression(compiler, position,
						CompileExceptionCode.SUPER_CALL_NO_SUPERCLASS,
						"Super call without superclass"));
	}

	private static class Compiling extends AbstractCompilingExpression implements ResolvedCallable {
		private final TypeID targetType;

		public Compiling(ExpressionCompiler compiler, CodePosition position, TypeID targetType) {
			super(compiler, position);
			this.targetType = targetType;
		}

		@Override
		public Expression as(TypeID type) {
			return compiler.at(position, type).invalid(
					CompileExceptionCode.NOT_AN_EXPRESSION,
					"super is not a valid expression");
		}

		@Override
		public TypeMatch matches(TypeID returnType) {
			return TypeMatch.NONE;
		}

		@Override
		public Optional<ResolvedCallable> call() {
			return Optional.of(this);
		}

		@Override
		public InferredType inferType() {
			return InferredType.failure(
					CompileExceptionCode.NOT_AN_EXPRESSION,
					"super is not a valid expression");
		}

		// #######################################
		// ### ResolvedCallable implementation ###
		// #######################################

		@Override
		public Expression call(TypeID returnType, CompilingExpression... arguments) {
			return compiler.resolve(targetType).getConstructor().superCall(arguments);
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
