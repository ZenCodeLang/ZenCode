package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;

public class ParsedExpressionPostCall extends ParsedExpression {
	private final ParsedExpression value;
	private final OperatorType operator;

	public ParsedExpressionPostCall(CodePosition position, ParsedExpression value, OperatorType operator) {
		super(position);

		this.value = value;
		this.operator = operator;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		Expression cValue = value.compile(scope).eval();
		return scope.getTypeMembers(cValue.type).getOrCreateGroup(operator).callPostfix(position, scope, cValue);
	}

	@Override
	public boolean hasStrongType() {
		return value.hasStrongType();
	}
}
