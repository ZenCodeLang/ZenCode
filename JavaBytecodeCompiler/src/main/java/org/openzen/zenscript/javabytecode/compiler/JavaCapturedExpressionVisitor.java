package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.expression.*;

public class JavaCapturedExpressionVisitor implements CapturedExpressionVisitor<Void> {

    public final JavaExpressionVisitor expressionVisitor;

    public JavaCapturedExpressionVisitor(JavaExpressionVisitor expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public Void visitCapturedThis(CapturedThisExpression expression) {
        return null;
    }

    @Override
    public Void visitCapturedParameter(CapturedParameterExpression expression) {
        return null;
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
