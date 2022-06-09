package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;

public interface StaticCallableMethod extends AnyMethod {
	Expression call(ExpressionBuilder builder, CallArguments arguments);

	boolean isImplicit();
}
