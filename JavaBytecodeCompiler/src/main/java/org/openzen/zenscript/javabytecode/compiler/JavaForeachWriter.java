package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaMethod;

import java.util.HashMap;
import java.util.Map;

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

		final int z = javaWriter.getLocalVariable(variables[0].variable).local;
		javaWriter.storeInt(z);
		javaWriter.label(startLabel);
		javaWriter.dup();
		javaWriter.loadInt(z);
		javaWriter.ifICmpLE(endLabel);

		content.accept(statementVisitor);
		javaWriter.iinc(z);
	}

	public void visitArrayValueIterator() {
		handleArray(javaWriter.local(int.class), javaWriter.getLocalVariable(variables[0].variable));
	}

	public void visitArrayKeyValueIterator() {
		handleArray(javaWriter.getLocalVariable(variables[0].variable).local, javaWriter.getLocalVariable(variables[1].variable));
	}

	public void visitStringCharacterIterator() {
		//TODO UNTESTED!
		javaWriter.invokeSpecial("java/lang/String", "toCharArray()", "()[C");
		handleArray(javaWriter.local(int.class), javaWriter.getLocalVariable(variables[0].variable));
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
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.MAP, "entrySet", "()Ljava/util/Set;", 0));
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.COLLECTION, "iterator", "()Ljava/util/Iterator;", 0));

		javaWriter.label(startLabel);
		javaWriter.dup();
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.ITERATOR, "hasNext", "()Z", 0));
		javaWriter.ifEQ(endLabel);
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.ITERATOR, "next", "()Ljava/lang/Object;", 0));
		javaWriter.checkCast(Type.getType(Map.Entry.class));
		javaWriter.dup(false);


		final JavaLocalVariableInfo keyVariable = javaWriter.getLocalVariable(variables[0].variable);
		final JavaLocalVariableInfo valueVariable = javaWriter.getLocalVariable(variables[1].variable);

		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.fromInternalName("java/util/Map$Entry", JavaClass.Kind.INTERFACE), "getKey", "()Ljava/lang/Object;", 0));
		javaWriter.store(keyVariable.type, keyVariable.local);

		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.fromInternalName("java/util/Map$Entry", JavaClass.Kind.INTERFACE), "getValue", "()Ljava/lang/Object;", 0));
		javaWriter.store(valueVariable.type, valueVariable.local);
		content.accept(statementVisitor);


		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
