package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.expression.Expression;

@FunctionalInterface
public interface TypedExpressionBuilder {
	Expression build(ExpressionBuilder builder);
}
