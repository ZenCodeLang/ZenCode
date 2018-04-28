package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.openzen.zenscript.codemodel.iterator.ForeachIteratorVisitor;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;

public class JavaForeachVisitor implements ForeachIteratorVisitor<Void> {

    private final JavaWriter javaWriter;
    private final VarStatement[] variables;
    private final Statement content;
    private final Label startLabel;
    private final Label endLabel;
    private final JavaStatementVisitor statementVisitor;

    public JavaForeachVisitor(JavaStatementVisitor statementVisitor, VarStatement[] variables, Statement content, Label start, Label end) {
        this.statementVisitor = statementVisitor;
        this.javaWriter = statementVisitor.getJavaWriter();
        this.variables = variables;
        this.content = content;
        this.startLabel = start;
        this.endLabel = end;
    }

    @Override
    public Void visitIntRange() {
        javaWriter.dup();
        javaWriter.getField("org/openzen/zenscript/implementations/IntRange", "max", "I");
        javaWriter.swap();
        javaWriter.getField("org/openzen/zenscript/implementations/IntRange", "min", "I");

        final int z = variables[0].getTag(JavaLocalVariableInfo.class).local;
        javaWriter.storeInt(z);
        javaWriter.label(startLabel);
        javaWriter.dup();
        javaWriter.loadInt(z);
        javaWriter.ifICmpLE(endLabel);

        content.accept(statementVisitor);
        javaWriter.iinc(z);


        return null;
    }

    @Override
    public Void visitArrayValueIterator() {
        handleArray(javaWriter.local(int.class), variables[0].getTag(JavaLocalVariableInfo.class));
        return null;
    }

    @Override
    public Void visitArrayKeyValueIterator() {
        handleArray(variables[0].getTag(JavaLocalVariableInfo.class).local, variables[1].getTag(JavaLocalVariableInfo.class));
        return null;
    }

    private void handleArray(final int z, final JavaLocalVariableInfo arrayTypeInfo) {
        javaWriter.iConst0();
        javaWriter.storeInt(z);

        javaWriter.label(startLabel);
        javaWriter.dup();
        javaWriter.dup();
        javaWriter.arrayLength();
        javaWriter.loadInt(z);

        javaWriter.ifICmpLE(endLabel);
        javaWriter.loadInt(z);


        javaWriter.arrayLoad(arrayTypeInfo.type);
        javaWriter.store(arrayTypeInfo.type, arrayTypeInfo.local);
        content.accept(statementVisitor);
        javaWriter.iinc(z);
    }

    @Override
    public Void visitCustomIterator() {
        return null;
    }
}
