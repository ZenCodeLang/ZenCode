package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.OrOrExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;

public class ParsedExpressionOrOr extends ParsedExpression {
	private final ParsedExpression left;
	private final ParsedExpression right;

	public ParsedExpressionOrOr(CodePosition position, ParsedExpression left, ParsedExpression right) {
		super(position);

		this.left = left;
		this.right = right;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		Expression cLeft = left.compile(scope.withHints(BasicTypeID.HINT_BOOL)).eval().castImplicit(position, scope, BasicTypeID.BOOL);
		Expression cRight = right.compile(scope.withHints(BasicTypeID.HINT_BOOL)).eval().castImplicit(position, scope, BasicTypeID.BOOL);
		return new OrOrExpression(position, cLeft, cRight);
	}

	@Override
	public boolean hasStrongType() {
		return left.hasStrongType() && right.hasStrongType();
	}
}
