package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.expression.*;

public class JavaCapturedExpressionVisitor implements CapturedExpressionVisitor<Void> {

	public final ExpressionVisitor<Void> expressionVisitor;

	public JavaCapturedExpressionVisitor(ExpressionVisitor<Void> expressionVisitor) {
		this.expressionVisitor = expressionVisitor;
	}

	@Override
	public Void visitCapturedThis(CapturedThisExpression expression) {
		return null;
	}

	@Override
	public Void visitCapturedParameter(CapturedParameterExpression expression) {
		return new GetFunctionParameterExpression(expression.position, expression.parameter).accept(expressionVisitor);
	}

	@Override
	public Void visitCapturedLocal(CapturedLocalVariableExpression expression) {
		return new GetLocalVariableExpression(expression.position, expression.variable)
				.accept(expressionVisitor);
	}

	@Override
	public Void visitCapturedDirect(CapturedDirectExpression expression) {
		return expression.value.accept(expressionVisitor);
	}

	@Override
	public Void visitRecaptured(CapturedClosureExpression expression) {
		return expression.value.accept(this);
	}
}
