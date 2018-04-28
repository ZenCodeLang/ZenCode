package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.statement.*;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;

public class JavaStatementVisitor implements StatementVisitor<Void> {
    private final JavaWriter javaWriter;
    private final JavaExpressionVisitor expressionVisitor;

    public JavaStatementVisitor(final JavaWriter javaWriter) {
        this.javaWriter = javaWriter;
        this.expressionVisitor = new JavaExpressionVisitor(javaWriter);
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
        javaWriter.goTo(javaWriter.getNamedLabel(statement.target.label + "_end"));
        return null;
    }

    @Override
    public Void visitContinue(ContinueStatement statement) {
        javaWriter.goTo(javaWriter.getNamedLabel(statement.target.label + "_start"));
        return null;
    }

    @Override
    public Void visitDoWhile(DoWhileStatement statement) {
        Label start = new Label();
        Label end = new Label();
        if (statement.label == null)
            statement.label = javaWriter.createLabelName() + "DoWhile";
        javaWriter.putNamedLabel(start, statement.label + "_start");
        javaWriter.putNamedLabel(end, statement.label + "_end");
        javaWriter.label(start);
        statement.content.accept(this);

        statement.condition.accept(expressionVisitor);
        javaWriter.ifNE(start);

        //Only needed for break statements, should be nop if not used
        javaWriter.label(end);
        return null;
    }

    @Override
    public Void visitEmpty(EmptyStatement statement) {
        //No-Op
        return null;
    }

    @Override
    public Void visitExpression(ExpressionStatement statement) {
        statement.expression.accept(expressionVisitor);
        return null;
    }

    @Override
    public Void visitForeach(ForeachStatement statement) {
        //Create Labels
        Label start = new Label();
        Label end = new Label();
        if (statement.label == null) {
            statement.label = javaWriter.createLabelName() + "ForEach";
        }
        javaWriter.putNamedLabel(start, statement.label + "_start");
        javaWriter.putNamedLabel(end, statement.label + "_end");


        //Compile Array/Collection
        statement.list.accept(expressionVisitor);

        //Create local variables
        for (VarStatement variable : statement.loopVariables) {
            final Type type = Type.getType(variable.type.accept(JavaTypeClassVisitor.INSTANCE));
            variable.setTag(JavaLocalVariableInfo.class, new JavaLocalVariableInfo(type, javaWriter.local(type)));
        }

        //javaWriter.label(min);
        statement.iterator.acceptForIterator(new JavaForeachVisitor(this, statement.loopVariables, statement.content, start, end));
        javaWriter.goTo(start);
        javaWriter.label(end);
        return null;
    }

    @Override
    public Void visitIf(IfStatement statement) {
        statement.condition.accept(expressionVisitor);
        Label onElse = null;
        Label end = new Label();
        final boolean hasElse = statement.onElse != null;
        if (hasElse) {
            onElse = new Label();
            javaWriter.ifEQ(onElse);
        } else {
            javaWriter.ifEQ(end);
        }
        statement.onThen.accept(this);
        if (hasElse) {
            javaWriter.goTo(end);
            javaWriter.label(onElse);
            statement.onElse.accept(this);
        }
        javaWriter.label(end);
        return null;
    }

    @Override
    public Void visitLock(LockStatement statement) {
        return null;
    }

    @Override
    public Void visitReturn(ReturnStatement statement) {
        statement.value.accept(expressionVisitor);
        javaWriter.returnType(Type.getType(statement.value.type.accept(JavaTypeClassVisitor.INSTANCE)));
        return null;
    }

    @Override
    public Void visitThrow(ThrowStatement statement) {
        statement.value.accept(expressionVisitor);
        javaWriter.aThrow();
        return null;
    }

    @Override
    public Void visitTryCatch(TryCatchStatement statement) {
        return null;
    }

    @Override
    public Void visitVar(VarStatement statement) {
        Type type = Type.getType(statement.type.accept(JavaTypeClassVisitor.INSTANCE));
        int local = javaWriter.local(type);
        if (statement.initializer != null) {
            statement.initializer.accept(expressionVisitor);
            javaWriter.store(type, local);
        }
        statement.setTag(JavaLocalVariableInfo.class, new JavaLocalVariableInfo(type, local));
        return null;
    }

    @Override
    public Void visitWhile(WhileStatement statement) {
        Label start = new Label();
        Label end = new Label();

        if (statement.label == null) {
            statement.label = javaWriter.createLabelName() + "WhileDo";
        }
        javaWriter.putNamedLabel(start, statement.label + "_start");
        javaWriter.putNamedLabel(end, statement.label + "_end");

        javaWriter.label(start);
        statement.condition.accept(expressionVisitor);
        javaWriter.ifEQ(end);
        statement.content.accept(this);
        javaWriter.goTo(start);
        javaWriter.label(end);
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
