package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.expression.PanicExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

public class ParsedPanicExpression extends ParsedExpression {
	public final ParsedExpression value;

	public ParsedPanicExpression(CodePosition position, ParsedExpression value) {
		super(position);

		this.value = value;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		return new PanicExpression(position, scope.getResultTypeHints().isEmpty() ? BasicTypeID.VOID : scope.getResultTypeHints().get(0), value.compile(scope).eval());
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
