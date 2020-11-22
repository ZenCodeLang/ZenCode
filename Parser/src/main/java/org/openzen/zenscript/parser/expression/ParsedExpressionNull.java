package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.NullExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;

public class ParsedExpressionNull extends ParsedExpression {
	public ParsedExpressionNull(CodePosition position) {
		super(position);
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		return new NullExpression(position);
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}
