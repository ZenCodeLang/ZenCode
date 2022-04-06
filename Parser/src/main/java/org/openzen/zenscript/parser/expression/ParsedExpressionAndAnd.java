package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.BinaryExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.expression.TypeMatch;

public class ParsedExpressionAndAnd extends ParsedExpression {
	private final ParsedExpression left;
	private final ParsedExpression right;

	public ParsedExpressionAndAnd(
			CodePosition position,
			ParsedExpression left,
			ParsedExpression right) {
		super(position);

		this.left = left;
		this.right = right;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, left.compile(compiler), right.compile(compiler));
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
			return compiler.at(position, type).binary(
					BinaryExpression.Operator.AND_AND,
					left.as(BasicTypeID.BOOL),
					right.as(BasicTypeID.BOOL));
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
