package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.types.BinaryOperator;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;
import org.openzen.zenscript.compiler.expression.ExpressionCompilerImpl;

import java.util.ArrayList;
import java.util.List;

public class ParsedExpressionBinary extends ParsedExpression {
	private final ParsedExpression left;
	private final ParsedExpression right;
	private final OperatorType operator;

	public ParsedExpressionBinary(CodePosition position, ParsedExpression left, ParsedExpression right, OperatorType operator) {
		super(position);

		this.left = left;
		this.right = right;
		this.operator = operator;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		Expression cLeft = left.compile(scope).eval();
		TypeMemberGroup members = scope.getTypeMembers(cLeft.type).getOrCreateGroup(this.operator);
		ExpressionScope innerScope = scope.withHints(members.predictCallTypes(position, scope, scope.getResultTypeHints(), 1)[0]);

		Expression cRight = right.compile(innerScope).eval();
		CallArguments arguments = new CallArguments(cRight);
		return members.call(position, scope, cLeft, arguments, false);
	}

	private class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression left;
		private final CompilingExpression right;
		private final OperatorType operator;

		public Compiling(ExpressionCompilerImpl compiler, CodePosition position, CompilingExpression left, CompilingExpression right, OperatorType operator) {
			super(compiler, position);
			this.left = left;
			this.right = right;
			this.operator = operator;
		}

		@Override
		public Expression as(TypeID type) {
			return inferOperandTypes(type)
					.map(binary -> {
						Expression cLeft = left.as(binary.left);
						compiler.resolve(cLeft.type)
								.findBinaryOperator(operator)
								.call(right.as(binary.right));

						return compiler.at(position, type).binary(
								binary.operator,
								left.as(binary.left),
								right.as(binary.right))
					})
					.orElseGet(() -> compiler.at(position, type).invalid(CompileExceptionCode.NO_SUCH_MEMBER, "Could not find a suitable operator member"));
		}

		@Override
		public InferredType inferType() {
			List<TypeID> inferred = new ArrayList<>();

			InferredType leftType = left.inferType(inferrer, hints);
			if (leftType.isFailed()) {
				return leftType;
			}
		}
	}
}
