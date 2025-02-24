package org.openzen.zenscript.javabytecode.compiler.capturing;

import org.openzen.zenscript.codemodel.expression.ExpressionVisitor;
import org.openzen.zenscript.codemodel.expression.GetFunctionParameterExpression;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.expression.captured.*;

public class JavaCapturedExpressionVisitorLoadIndyCapturesVisitor implements CapturedExpressionVisitor<Void> {
	private final ExpressionVisitor<Void> visitor;
	private final boolean onlyThis;

	public JavaCapturedExpressionVisitorLoadIndyCapturesVisitor(final ExpressionVisitor<Void> visitor, final boolean onlyThis) {
		this.visitor = visitor;
		this.onlyThis = onlyThis;
	}

	@Override
	public Void visitCapturedThis(final CapturedThisExpression expression) {
		if (!this.onlyThis) {
			return null;
		}
		return new ThisExpression(expression.position, expression.type).accept(this.visitor);
	}

	@Override
	public Void visitCapturedParameter(final CapturedParameterExpression expression) {
		if (this.onlyThis) {
			return null;
		}
		return new GetFunctionParameterExpression(expression.position, expression.parameter).accept(this.visitor);
	}

	@Override
	public Void visitCapturedLocal(final CapturedLocalVariableExpression expression) {
		if (this.onlyThis) {
			return null;
		}
		return new GetLocalVariableExpression(expression.position, expression.variable).accept(this.visitor);
	}

	@Override
	public Void visitRecaptured(final CapturedClosureExpression expression) {
		if (this.onlyThis) {
			return null;
		}
		return expression.value.accept(this.visitor);
	}
}
