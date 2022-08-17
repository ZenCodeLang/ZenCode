package org.openzen.zenscript.codemodel.expression;

public interface CapturedExpressionVisitor<T> {
	T visitCapturedThis(CapturedThisExpression expression);

	T visitCapturedParameter(CapturedParameterExpression expression);

	T visitCapturedLocal(CapturedLocalVariableExpression expression);

	T visitRecaptured(CapturedClosureExpression expression);
}
