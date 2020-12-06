package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedExpressionSuper extends ParsedExpression {
	public ParsedExpressionSuper(CodePosition position) {
		super(position);
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		TypeID type = scope.getThisType();
		TypeID targetType = type.getSuperType(scope.getTypeRegistry());
		if (targetType == null)
			throw new CompileException(position, CompileExceptionCode.SUPER_CALL_NO_SUPERCLASS, "Type has no superclass");

		return new PartialTypeExpression(position, targetType, null);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
