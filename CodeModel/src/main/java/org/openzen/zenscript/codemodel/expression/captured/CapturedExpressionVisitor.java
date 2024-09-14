package org.openzen.zenscript.codemodel.expression.captured;

public interface CapturedExpressionVisitor<T> {
	T visitCapturedThis(CapturedThisExpression expression);

	T visitCapturedParameter(CapturedParameterExpression expression);

	T visitCapturedLocal(CapturedLocalVariableExpression expression);

	T visitRecaptured(CapturedClosureExpression expression);
}
