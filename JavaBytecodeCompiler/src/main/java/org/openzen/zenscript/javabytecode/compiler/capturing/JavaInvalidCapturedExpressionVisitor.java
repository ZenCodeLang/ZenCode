package org.openzen.zenscript.javabytecode.compiler.capturing;

import org.openzen.zenscript.codemodel.expression.captured.*;

/**
 * Fallback {@link CapturedExpressionVisitor} used by the JavaExpressionVisitor whenever we are outside any lambda context
 */
public class JavaInvalidCapturedExpressionVisitor implements CapturedExpressionVisitor<Void> {

	@Override
	public Void visitCapturedThis(CapturedThisExpression expression) {
		throw unsupportedOperation();
	}

	@Override
	public Void visitCapturedParameter(CapturedParameterExpression expression) {
		throw unsupportedOperation();
	}

	@Override
	public Void visitCapturedLocal(CapturedLocalVariableExpression expression) {
		throw unsupportedOperation();
	}

	@Override
	public Void visitRecaptured(CapturedClosureExpression expression) {
		throw unsupportedOperation();
	}

	private static UnsupportedOperationException unsupportedOperation() {
		return new UnsupportedOperationException("Not inside a lambda!");
	}
}
