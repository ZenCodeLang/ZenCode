package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.statement.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.javabytecode.JavaClassInfo;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javabytecode.JavaMethodInfo;

import java.util.Arrays;
import java.util.List;

public class JavaStatementVisitor implements StatementVisitor<Boolean> {
    private final JavaWriter javaWriter;
    public final JavaExpressionVisitor expressionVisitor;

    /**
     * @param javaWriter the method writer that compiles the statement
     */
    public JavaStatementVisitor(JavaWriter javaWriter) {
        this.javaWriter = javaWriter;
        this.expressionVisitor = new JavaExpressionVisitor(javaWriter);
    }

    @Override
    public Boolean visitBlock(BlockStatement statement) {
        Boolean returns = false;
        for (Statement statement1 : statement.statements) {
            returns = statement1.accept(this);
        }
        return returns;
    }

    @Override
    public Boolean visitBreak(BreakStatement statement) {
        javaWriter.goTo(javaWriter.getNamedLabel(statement.target.label + "_end"));
        return false;
    }

    @Override
    public Boolean visitContinue(ContinueStatement statement) {
        javaWriter.goTo(javaWriter.getNamedLabel(statement.target.label + "_start"));
        return false;
    }

    @Override
    public Boolean visitDoWhile(DoWhileStatement statement) {
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
        return false;
    }

    @Override
    public Boolean visitEmpty(EmptyStatement statement) {
        //No-Op
        return false;
    }

    @Override
    public Boolean visitExpression(ExpressionStatement statement) {
        statement.expression.accept(expressionVisitor);
        return false;
    }

    @Override
    public Boolean visitForeach(ForeachStatement statement) {
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
        return false;
    }

    @Override
    public Boolean visitIf(IfStatement statement) {
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
        return false;
    }

    @Override
    public Boolean visitLock(LockStatement statement) {
        return false;
    }

    @Override
    public Boolean visitReturn(ReturnStatement statement) {
        statement.value.accept(expressionVisitor);
        javaWriter.returnType(Type.getType(statement.value.type.accept(JavaTypeClassVisitor.INSTANCE)));
        return true;
    }

    @Override
    public Boolean visitSwitch(SwitchStatement statement) {

        final Label start = new Label();
        final Label end = new Label();

        if (statement.label == null)
            statement.label = javaWriter.createLabelName() + "Switch";

        javaWriter.putNamedLabel(start, statement.label + "_start");
        javaWriter.putNamedLabel(end, statement.label + "_end");


        javaWriter.label(start);
        statement.value.accept(expressionVisitor);
        if(statement.value.type == BasicTypeID.STRING)
            javaWriter.invokeVirtual(new JavaMethodInfo(new JavaClassInfo("java/lang/Object"), "hashCode", "()I", 0));
        boolean out = false;

        final boolean hasNoDefault = hasNoDefault(statement);

        final List<SwitchCase> cases = statement.cases;
		final JavaSwitchLabel[] switchLabels = new JavaSwitchLabel[hasNoDefault ? cases.size() : cases.size() - 1];
        final Label defaultLabel = new Label();
		
        int i = 0;
        for (final SwitchCase switchCase : cases) {
            if (switchCase.value != null) {
				switchLabels[i++] = new JavaSwitchLabel(CompilerUtils.getKeyForSwitch(switchCase.value), new Label());
			}
		}
		
		JavaSwitchLabel[] sortedSwitchLabels = Arrays.copyOf(switchLabels, switchLabels.length);
		Arrays.sort(sortedSwitchLabels, (a, b) -> a.key - b.key);

        javaWriter.lookupSwitch(defaultLabel, sortedSwitchLabels);

        i = 0;
        for (final SwitchCase switchCase : cases) {
            if (hasNoDefault || switchCase.value != null) {
                javaWriter.label(switchLabels[i++].label);
            } else {
                javaWriter.label(defaultLabel);
            }
            for (Statement statement1 : switchCase.statements) {
                out |= statement1.accept(this);
            }
        }

        if(hasNoDefault)
            javaWriter.label(defaultLabel);

        javaWriter.label(end);


        //throw new UnsupportedOperationException("Not yet implemented!");
        return out;
    }

    private boolean hasNoDefault(SwitchStatement switchStatement) {
        for (SwitchCase switchCase : switchStatement.cases)
            if (switchCase.value == null) return false;
        return true;
	}

    @Override
    public Boolean visitThrow(ThrowStatement statement) {
        statement.value.accept(expressionVisitor);
        javaWriter.aThrow();
        return false;
    }

    @Override
    public Boolean visitTryCatch(TryCatchStatement statement) {
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

        return false;
    }

    @Override
    public Boolean visitVar(VarStatement statement) {
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
        return false;
    }

    @Override
    public Boolean visitWhile(WhileStatement statement) {
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
        return false;
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
