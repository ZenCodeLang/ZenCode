package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;

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
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		Expression cLeft = left.compile(scope.withoutHints()).eval();
		Expression cRight = right.compile(scope.withHint(cLeft.type)).eval();
		return scope.getTypeMembers(cLeft.type).compare(position, scope, type, cLeft, cRight);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
