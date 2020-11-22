package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;

public class ParsedExpressionThis extends ParsedExpression {
	public ParsedExpressionThis(CodePosition position) {
		super(position);
	}
	
	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		return new ThisExpression(position, scope.getThisType());
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
