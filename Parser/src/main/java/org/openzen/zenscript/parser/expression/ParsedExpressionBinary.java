package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.Expression;

public class ParsedExpressionBinary extends ParsedExpression {
	private final CompilableExpression left;
	private final CompilableExpression right;
	private final OperatorType operator;

	public ParsedExpressionBinary(CodePosition position, CompilableExpression left, CompilableExpression right, OperatorType operator) {
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

	public static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression left;
		private final Expression leftValue;
		private final CompilingExpression right;
		private final OperatorType operator;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression left, CompilingExpression right, OperatorType operator) {
			super(compiler, position);
			this.left = left;
			this.leftValue = left.eval();
			this.right = right;
			this.operator = operator;
		}

		@Override
		public Expression eval() {
			ResolvedType resolved = compiler.resolve(leftValue.type);
			return resolved.findOperator(operator)
					.map(operator -> operator.call(compiler.at(position), leftValue, right))
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noOperatorInType(leftValue.type, operator)));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			ResolvedType resolved = compiler.resolve(leftValue.type);
			return resolved.findOperator(operator)
					.map(operator -> operator.cast(compiler.at(position), cast, leftValue, right))
					.orElse(cast.invalid(CompileErrors.noOperatorInType(leftValue.type, operator)));
		}
	}
}
