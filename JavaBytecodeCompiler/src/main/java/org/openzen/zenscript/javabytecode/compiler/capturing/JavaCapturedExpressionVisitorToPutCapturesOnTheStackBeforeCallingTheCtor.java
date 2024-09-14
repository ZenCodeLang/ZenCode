package org.openzen.zenscript.javabytecode.compiler.capturing;

import org.openzen.zenscript.codemodel.expression.ExpressionVisitor;
import org.openzen.zenscript.codemodel.expression.GetFunctionParameterExpression;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.expression.captured.*;

public class JavaCapturedExpressionVisitorToPutCapturesOnTheStackBeforeCallingTheCtor implements CapturedExpressionVisitor<Void> {
	private final ExpressionVisitor<Void> expressionVisitor;

	public JavaCapturedExpressionVisitorToPutCapturesOnTheStackBeforeCallingTheCtor(ExpressionVisitor<Void> expressionVisitor) {
		this.expressionVisitor = expressionVisitor;
	}

	@Override
	public Void visitCapturedThis(CapturedThisExpression expression) {
		return new ThisExpression(expression.position, expression.type).accept(expressionVisitor);
	}

	@Override
	public Void visitCapturedParameter(CapturedParameterExpression expression) {
		return new GetFunctionParameterExpression(expression.position, expression.parameter).accept(expressionVisitor);
	}

	@Override
	public Void visitCapturedLocal(CapturedLocalVariableExpression expression) {
		return new GetLocalVariableExpression(expression.position, expression.variable).accept(expressionVisitor);
	}

	@Override
	public Void visitRecaptured(CapturedClosureExpression expression) {
		return expression.value.accept(expressionVisitor);
	}
}
