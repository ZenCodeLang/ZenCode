package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;

public interface InstanceCallableMethod extends AnyMethod {
	Modifiers getModifiers();

	Expression call(ExpressionBuilder builder, Expression instance, CallArguments arguments);

	Expression callPostfix(ExpressionBuilder builder, Expression instance);
}
