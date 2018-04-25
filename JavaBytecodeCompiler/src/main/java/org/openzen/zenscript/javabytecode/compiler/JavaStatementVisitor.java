package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.statement.*;

public class JavaStatementVisitor implements StatementVisitor<Void> {
    private final JavaWriter javaWriter;
    private final JavaExpressionVisitor expressionVisitor;

    public JavaStatementVisitor(final JavaWriter javaWriter) {

        this.javaWriter = javaWriter;
        this.expressionVisitor = new JavaExpressionVisitor( javaWriter);
    }

    @Override
    public Void visitBlock(BlockStatement statement) {
        for (Statement statement1 : statement.statements) {
            statement1.accept(this);
        }
        return null;
    }

    @Override
    public Void visitBreak(BreakStatement statement) {
        return null;
    }

    @Override
    public Void visitContinue(ContinueStatement statement) {
        return null;
    }

    @Override
    public Void visitDoWhile(DoWhileStatement statement) {
        return null;
    }

    @Override
    public Void visitEmpty(EmptyStatement statement) {
        return null;
    }

    @Override
    public Void visitExpression(ExpressionStatement statement) {
        statement.expression.accept(expressionVisitor);
        return null;
    }

    @Override
    public Void visitForeach(ForeachStatement statement) {
        return null;
    }

    @Override
    public Void visitIf(IfStatement statement) {
        return null;
    }

    @Override
    public Void visitLock(LockStatement statement) {
        return null;
    }

    @Override
    public Void visitReturn(ReturnStatement statement) {
        return null;
    }

    @Override
    public Void visitThrow(ThrowStatement statement) {
        return null;
    }

    @Override
    public Void visitTryCatch(TryCatchStatement statement) {
        return null;
    }

    @Override
    public Void visitVar(VarStatement statement) {
        return null;
    }

    @Override
    public Void visitWhile(WhileStatement statement) {
        return null;
    }

    public void start() {
        javaWriter.start();
    }

    public void end() {
        javaWriter.ret();
        javaWriter.end();
    }

    public JavaWriter getJavaWriter() {
        return javaWriter;
    }
}
