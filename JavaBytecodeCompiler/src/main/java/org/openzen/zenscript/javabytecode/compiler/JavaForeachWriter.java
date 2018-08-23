package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;

public class JavaForeachWriter {

	private final JavaWriter javaWriter;
	private final VarStatement[] variables;
	private final Statement content;
	private final Label startLabel;
	private final Label endLabel;
	private final JavaStatementVisitor statementVisitor;

	public JavaForeachWriter(JavaStatementVisitor statementVisitor, VarStatement[] variables, Statement content, Label start, Label end) {
		this.statementVisitor = statementVisitor;
		this.javaWriter = statementVisitor.getJavaWriter();
		this.variables = variables;
		this.content = content;
		this.startLabel = start;
		this.endLabel = end;
	}
	
	public void visitIntRange() {
		javaWriter.dup();
		javaWriter.getField("zsynthetic/IntRange", "to", "I");
		javaWriter.swap();
		javaWriter.getField("zsynthetic/IntRange", "from", "I");

		final int z = variables[0].getTag(JavaLocalVariableInfo.class).local;
		javaWriter.storeInt(z);
		javaWriter.label(startLabel);
		javaWriter.dup();
		javaWriter.loadInt(z);
		javaWriter.ifICmpLE(endLabel);

		content.accept(statementVisitor);
		javaWriter.iinc(z);
	}

	public void visitArrayValueIterator() {
		handleArray(javaWriter.local(int.class), variables[0].getTag(JavaLocalVariableInfo.class));
	}

	public void visitArrayKeyValueIterator() {
		handleArray(variables[0].getTag(JavaLocalVariableInfo.class).local, variables[1].getTag(JavaLocalVariableInfo.class));
	}

	public void visitStringCharacterIterator() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

	public void visitCustomIterator() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void visitAssocKeyIterator() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void visitAssocKeyValueIterator() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
