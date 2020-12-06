package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ThrowExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedThrowExpression extends ParsedExpression {
	public final ParsedExpression value;

	public ParsedThrowExpression(CodePosition position, ParsedExpression value) {
		super(position);

		this.value = value;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		Expression cValue = value.compile(scope).eval();
		TypeID resultType = BasicTypeID.VOID;
		if (scope.getResultTypeHints().size() == 1)
			resultType = scope.getResultTypeHints().get(0);

		return new ThrowExpression(position, resultType, cValue);
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}
