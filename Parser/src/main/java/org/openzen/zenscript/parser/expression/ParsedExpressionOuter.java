package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedExpressionOuter extends ParsedExpression {
	private final ParsedExpression value;

	public ParsedExpressionOuter(CodePosition position, ParsedExpression value) {
		super(position);

		this.value = value;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		TypeID thisType = scope.getThisType();
		if (!(thisType instanceof DefinitionTypeID))
			throw new CompileException(position, CompileExceptionCode.USING_THIS_OUTSIDE_TYPE, "Not in a type");

		return scope.getOuterInstance(position);
	}

	@Override
	public boolean hasStrongType() {
		return value.hasStrongType();
	}
}
