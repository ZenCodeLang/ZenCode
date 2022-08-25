package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedExpressionOpAssign extends ParsedExpression {
	private final CompilableExpression left;
	private final CompilableExpression right;
	private final OperatorType operator;

	public ParsedExpressionOpAssign(CodePosition position, CompilableExpression left, CompilableExpression right, OperatorType operator) {
		super(position);

		this.left = left;
		this.right = right;
		this.operator = operator;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		CompilingExpression left = this.left.compile(compiler);
		CompilingExpression right = this.right.compile(compiler);
		return new Compiling(compiler, position, left, right, operator);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression left;
		private final CompilingExpression right;
		private final OperatorType operator;

		public Compiling(
				ExpressionCompiler compiler,
				CodePosition position,
				CompilingExpression left,
				CompilingExpression right,
				OperatorType operator
		) {
			super(compiler, position);

			this.left = left;
			this.right = right;
			this.operator = operator;
		}

		@Override
		public Expression eval() {
			Expression left = this.left.eval();
			ResolvedType resolvedType = compiler.resolve(left.type);
			return resolvedType.findOperator(operator.assignOperatorFor)
					.map(method -> method.call(compiler, position, left, TypeID.NONE, right))
					.orElseGet(() -> this.left.assign(new ParsedExpressionBinary.Compiling(compiler, position, this.left, right, operator)).eval());
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			CastedExpression left = this.left.cast(cast);
			ResolvedType resolvedType = compiler.resolve(left.value.type);
			return resolvedType.findOperator(operator.assignOperatorFor)
					.map(method -> method.cast(compiler, position, cast, left.value, TypeID.NONE, right))
					.orElseGet(() -> this.left.assign(new ParsedExpressionBinary.Compiling(compiler, position, this.left, right, operator)).cast(cast));
		}
	}
}
