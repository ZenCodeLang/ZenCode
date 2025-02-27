package org.openzen.zenscript.javabytecode.compiler.lambda.capturing;

import org.openzen.zenscript.codemodel.expression.ExpressionVisitor;
import org.openzen.zenscript.codemodel.expression.GetFunctionParameterExpression;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.captured.*;

public final class JavaLoadCapturesOnIndyCapturedExpressionVisitor implements CapturedExpressionVisitor<Void> {
	private final ExpressionVisitor<Void> visitor;

	public JavaLoadCapturesOnIndyCapturedExpressionVisitor(final ExpressionVisitor<Void> visitor) {
		this.visitor = visitor;
	}

	@Override
	public Void visitCapturedThis(final CapturedThisExpression expression) {
		return null;
	}

	@Override
	public Void visitCapturedParameter(final CapturedParameterExpression expression) {
		return new GetFunctionParameterExpression(expression.position, expression.parameter).accept(this.visitor);
	}

	@Override
	public Void visitCapturedLocal(final CapturedLocalVariableExpression expression) {
		return new GetLocalVariableExpression(expression.position, expression.variable).accept(this.visitor);
	}

	@Override
	public Void visitRecaptured(final CapturedClosureExpression expression) {
		return expression.value.accept(this.visitor);
	}
}
