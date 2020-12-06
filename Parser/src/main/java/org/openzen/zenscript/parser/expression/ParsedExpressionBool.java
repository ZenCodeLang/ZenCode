package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.ConstantBoolExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;

public class ParsedExpressionBool extends ParsedExpression {
	private final boolean value;

	public ParsedExpressionBool(CodePosition position, boolean value) {
		super(position);

		this.value = value;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		return new ConstantBoolExpression(position, value);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
