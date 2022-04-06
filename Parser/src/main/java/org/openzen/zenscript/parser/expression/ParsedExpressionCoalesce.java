package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;
import org.openzen.zenscript.compiler.expression.TypeMatch;

public class ParsedExpressionCoalesce extends ParsedExpression {
	private final ParsedExpression left;
	private final ParsedExpression right;

	public ParsedExpressionCoalesce(CodePosition position, ParsedExpression left, ParsedExpression right) {
		super(position);

		this.left = left;
		this.right = right;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		CompilingExpression left = this.left.compile(compiler);
		CompilingExpression right = this.right.compile(compiler);
		return new Compiling(compiler, position, left, right);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression left;
		private final CompilingExpression right;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression left, CompilingExpression right) {
			super(compiler, position);
			this.left = left;
			this.right = right;
		}

		@Override
		public Expression as(TypeID type) {
			Expression cLeft = left.as(compiler.types().optionalOf(type));
			TypeID rightType = cLeft.type.withoutOptional();
			Expression cRight = right.as(rightType);
			return compiler.at(position, type).coalesce(cLeft, cRight);
		}

		@Override
		public TypeMatch matches(TypeID returnType) {
			return TypeMatch.min(
					left.matches(compiler.types().optionalOf(returnType)),
					right.matches(returnType));
		}

		@Override
		public InferredType inferType() {
			InferredType leftType = left.inferType();
			if (leftType.isFailed())
				return leftType;

			InferredType rightType = right.inferType();
			if (rightType.isFailed())
				return rightType;

			return compiler.union(leftType.get().withoutOptional(), rightType.get())
					.map(InferredType::success)
					.orElseGet(() -> InferredType.failure(CompileExceptionCode.TYPE_CANNOT_UNITE, "The types " + leftType.get() + " and " + rightType.get() + " are unrelated"));
		}
	}
}
