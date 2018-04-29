package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.statement.*;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;

public class JavaStatementVisitor implements StatementVisitor<Void> {
    private final JavaWriter javaWriter;
    public final JavaExpressionVisitor expressionVisitor;
    private final boolean isInit;

    public JavaStatementVisitor(final JavaWriter javaWriter) {
        this(javaWriter, false);
    }

    /**
     * @param javaWriter the method writer that compiles the statement
     * @param isInit is the method a class initializer
     */
    public JavaStatementVisitor(JavaWriter javaWriter, boolean isInit) {
        this.javaWriter = javaWriter;
        this.expressionVisitor = new JavaExpressionVisitor(javaWriter, isInit);
        this.isInit = isInit;
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
            final Label variableStart = new Label();
            final JavaLocalVariableInfo info = new JavaLocalVariableInfo(type, javaWriter.local(type), variableStart, variable.name);
            info.end = end;
            variable.setTag(JavaLocalVariableInfo.class, info);
            javaWriter.addVariableInfo(info);
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
        final Label tryCatchStart = new Label();
        final Label tryFinish = new Label();
        final Label tryCatchFinish = new Label();
        final Label finallyStart = new Label();

        javaWriter.label(tryCatchStart);
        //TODO Check for returns or breaks out of the try-catch and inject finally block before them
        statement.content.accept(this);
        javaWriter.label(tryFinish);
        if (statement.finallyClause != null)
            statement.finallyClause.accept(this);
        javaWriter.goTo(tryCatchFinish);

        for (CatchClause catchClause : statement.catchClauses) {
            final Label catchStart = new Label();
            javaWriter.label(catchStart);

            //final Type exceptionType = Type.getType(RuntimeException.class);
            final Type exceptionType = Type.getType(catchClause.exceptionType.accept(JavaTypeClassVisitor.INSTANCE));
            final int local = javaWriter.local(exceptionType);
            javaWriter.store(exceptionType, local);

            catchClause.content.accept(this);
            final Label catchFinish = new Label();
            javaWriter.label(catchFinish);

            if (statement.finallyClause != null) {
                statement.finallyClause.accept(this);
                javaWriter.tryCatch(catchStart, catchFinish, finallyStart, null);
            }

            javaWriter.tryCatch(tryCatchStart, tryFinish, catchStart, exceptionType.getInternalName());
            javaWriter.goTo(tryCatchFinish);
        }

        if (statement.finallyClause != null) {
            javaWriter.label(finallyStart);
            final int local = javaWriter.local(Object.class);
            javaWriter.storeObject(local);
            statement.finallyClause.accept(this);
            javaWriter.loadObject(local);
            javaWriter.aThrow();
            javaWriter.tryCatch(tryCatchStart, tryFinish, finallyStart, null);
        }
        javaWriter.label(tryCatchFinish);

        return null;
    }

    @Override
    public Void visitVar(VarStatement statement) {
        Type type = statement.type.accept(JavaTypeVisitor.INSTANCE);
        int local = javaWriter.local(type);
        if (statement.initializer != null) {
            statement.initializer.accept(expressionVisitor);
            javaWriter.store(type, local);
        }
        final Label variableStart = new Label();
        javaWriter.label(variableStart);
        final JavaLocalVariableInfo info = new JavaLocalVariableInfo(type, local, variableStart, statement.name);
        statement.setTag(JavaLocalVariableInfo.class, info);
        javaWriter.addVariableInfo(info);
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
