package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;
import org.openzen.zenscript.compiler.expression.TypeMatch;

public class ParsedExpressionCompare extends ParsedExpression {
	private final ParsedExpression left;
	private final ParsedExpression right;
	private final CompareType type;

	public ParsedExpressionCompare(
			CodePosition position,
			ParsedExpression left,
			ParsedExpression right,
			CompareType type) {
		super(position);

		this.left = left;
		this.right = right;
		this.type = type;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		CompilingExpression left = this.left.compile(compiler);
		CompilingExpression right = this.right.compile(compiler);
		return new Compiling(compiler, position, left, right, type);
	}

	private class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression left;
		private final CompilingExpression right;
		private final CompareType type;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression left, CompilingExpression right, CompareType type) {
			super(compiler, position);
			this.left = left;
			this.right = right;
			this.type = type;
		}

		@Override
		public Expression as(TypeID type) {
			InferredType leftType = left.inferType();
			if (leftType.isFailed())
				return compiler.at(position, type).invalid(leftType.getErrorCode(), leftType.getErrorMessage());

			Expression left = this.left.as(leftType.get());
			// TODO: infer right type from left type comparators
			InferredType rightType = right.inferType();
			if (rightType.isFailed())
				return compiler.at(position, type).invalid(rightType.getErrorCode(), rightType.getErrorMessage());

			Expression right = this.right.as(rightType.get());
			return compiler.resolve(left.type).compare(right.type)
					.map(comparator -> comparator.compare(position, left, right, this.type))
					.orElseGet(() -> compiler.at(position, type)
							.invalid(CompileExceptionCode.NO_SUCH_MEMBER, "No comparator available to compare " + left.type + " with " + right.type));
		}

		@Override
		public TypeMatch matches(TypeID returnType) {
			return compiler.matchType(BasicTypeID.BOOL, returnType);
		}

		@Override
		public InferredType inferType() {
			return InferredType.success(BasicTypeID.BOOL);
		}
	}
}
