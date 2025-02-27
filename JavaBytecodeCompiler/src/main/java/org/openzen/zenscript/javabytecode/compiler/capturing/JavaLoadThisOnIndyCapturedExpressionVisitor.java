package org.openzen.zenscript.javabytecode.compiler.capturing;

import org.openzen.zenscript.codemodel.expression.ExpressionVisitor;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.expression.captured.*;

public final class JavaLoadThisOnIndyCapturedExpressionVisitor implements CapturedExpressionVisitor<Boolean> {
	private final ExpressionVisitor<Void> visitor;

	public JavaLoadThisOnIndyCapturedExpressionVisitor(final ExpressionVisitor<Void> visitor) {
		this.visitor = visitor;
	}

	@Override
	public Boolean visitCapturedThis(final CapturedThisExpression expression) {
		new ThisExpression(expression.position, expression.type).accept(this.visitor);
		return true;
	}

	@Override
	public Boolean visitCapturedParameter(final CapturedParameterExpression expression) {
		return false;
	}

	@Override
	public Boolean visitCapturedLocal(final CapturedLocalVariableExpression expression) {
		return false;
	}

	@Override
	public Boolean visitRecaptured(final CapturedClosureExpression expression) {
		return false;
	}
}
