package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.expression.TypeMatch;

import java.util.Optional;

public class ParsedExpressionAssign extends ParsedExpression {
	private final ParsedExpression left;
	private final ParsedExpression right;

	public ParsedExpressionAssign(CodePosition position, ParsedExpression left, ParsedExpression right) {
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
			return left.assign(right.as(type));
		}

		@Override
		public Expression assign(Expression value) {
			return left.assign(right.assign(value));
		}

		@Override
		public TypeMatch matches(TypeID returnType) {
			TypeMatch leftMatch = left.matches(returnType);
			TypeMatch rightMatch = right.matches(returnType);
			return TypeMatch.min(leftMatch, rightMatch);
		}

		@Override
		public InferredType inferType() {
			Optional<TypeID> fromLeft = left.inferAssignType();
			if (fromLeft.isPresent()) {
				if (right.matches(fromLeft.get()) != TypeMatch.NONE)
					return InferredType.success(fromLeft.get());
			}

			return right.inferType();
		}

		@Override
		public Optional<TypeID> inferAssignType() {
			return left.inferAssignType();
		}
	}
}
