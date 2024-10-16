package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.statement.ForeachStatement;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaModifiers;

import java.util.Map;

@SuppressWarnings("Duplicates")
public class JavaForeachWriter {

	private final JavaWriter javaWriter;
	private final ForeachStatement statement;
	private final Label startLabel;
	private final Label endLabel;
	private final JavaStatementVisitor statementVisitor;
	private final JavaUnboxingTypeVisitor unboxingTypeVisitor;

	public JavaForeachWriter(JavaStatementVisitor statementVisitor, ForeachStatement statement, Label start, Label end) {
		this.statementVisitor = statementVisitor;
		this.javaWriter = statementVisitor.getJavaWriter();
		this.statement = statement;
		this.startLabel = start;
		this.endLabel = end;
		this.unboxingTypeVisitor = new JavaUnboxingTypeVisitor(this.javaWriter);
	}

	public void visitIntRange(RangeTypeID type) {
		final String owner = statementVisitor.context.getInternalName(type);
		javaWriter.dup();
		javaWriter.getField(owner, "to", "I");
		javaWriter.swap();
		javaWriter.getField(owner, "from", "I");

		final int z = javaWriter.getLocalVariable(statement.loopVariables[0].variable).local;
		javaWriter.storeInt(z);
		javaWriter.label(startLabel);
		javaWriter.dup();
		javaWriter.loadInt(z);
		javaWriter.ifICmpLE(endLabel);

		statement.content.accept(statementVisitor);
		javaWriter.iinc(z);
	}

	public void visitArrayValueIterator() {
		handleArray(javaWriter.local(int.class), javaWriter.getLocalVariable(statement.loopVariables[0].variable));
	}

	public void visitArrayKeyValueIterator() {
		handleArray(javaWriter.getLocalVariable(statement.loopVariables[0].variable).local, javaWriter.getLocalVariable(statement.loopVariables[1].variable));
	}

	public void visitStringCharacterIterator() {
		javaWriter.invokeVirtual(JavaMethod.getVirtual(JavaClass.STRING, "toCharArray", "()[C", JavaModifiers.PUBLIC));
		handleArray(javaWriter.local(int.class), javaWriter.getLocalVariable(statement.loopVariables[0].variable));
	}

	public void visitIteratorIterator(Type targetType) {
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.ITERABLE, "iterator", "()Ljava/lang/Iterator;", 0));
		javaWriter.label(startLabel);
		javaWriter.dup();
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.ITERATOR, "hasNext", "()Z", 0));
		javaWriter.ifEQ(endLabel);
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.ITERATOR, "next", "()Ljava/lang/Object;", 0, true));
		javaWriter.checkCast(targetType);
		final JavaLocalVariableInfo variable = javaWriter.getLocalVariable(statement.loopVariables[0].variable);
		javaWriter.store(variable.type, variable.local);

		statement.content.accept(statementVisitor);
	}

	private void handleArray(final int z, final JavaLocalVariableInfo arrayTypeInfo) {
		javaWriter.iConst0();
		javaWriter.storeInt(z);

		javaWriter.label(startLabel);
		javaWriter.dup();
		javaWriter.arrayLength();
		javaWriter.loadInt(z);

		javaWriter.ifICmpLE(endLabel);
		javaWriter.dup();
		javaWriter.loadInt(z);

		ArrayTypeID listType = (ArrayTypeID) statement.list.type;
		if (listType.elementType == BasicTypeID.BYTE) {
			javaWriter.arrayLoad(Type.BYTE_TYPE);
			javaWriter.siPush((short) 255);
			javaWriter.iAnd();
		} else if (listType.elementType == BasicTypeID.USHORT) {
			javaWriter.arrayLoad(Type.SHORT_TYPE);
			javaWriter.constant(0xFFFF);
			javaWriter.iAnd();
		} else {
			javaWriter.arrayLoad(arrayTypeInfo.type);
		}
		javaWriter.store(arrayTypeInfo.type, arrayTypeInfo.local);
		statement.content.accept(statementVisitor);
		javaWriter.iinc(z);
	}

	public void visitCustomIterator() {
		javaWriter.invokeInterface(JavaMethod.getVirtual(new JavaClass("java.lang", "Iterable", JavaClass.Kind.INTERFACE), "iterator", "()Ljava/util/Iterator;", 0));

		javaWriter.label(startLabel);
		javaWriter.dup();
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.ITERATOR, "hasNext", "()Z", 0));
		javaWriter.ifEQ(endLabel);
		javaWriter.dup();
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.ITERATOR, "next", "()Ljava/lang/Object;", 0, true));

		final JavaLocalVariableInfo keyVariable = javaWriter.getLocalVariable(statement.loopVariables[0].variable);
		this.downCast(0, keyVariable.type);
		javaWriter.store(keyVariable.type, keyVariable.local);

		statement.content.accept(statementVisitor);
	}

	public void visitAssocKeyIterator() {
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.MAP, "keySet", "()Ljava/util/Set;", 0));
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.COLLECTION, "iterator", "()Ljava/util/Iterator;", 0));

		javaWriter.label(startLabel);
		javaWriter.dup();
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.ITERATOR, "hasNext", "()Z", 0));
		javaWriter.ifEQ(endLabel);
		javaWriter.dup();
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.ITERATOR, "next", "()Ljava/lang/Object;", 0, true));

		final JavaLocalVariableInfo keyVariable = javaWriter.getLocalVariable(statement.loopVariables[0].variable);
		this.downCast(0, keyVariable.type);
		javaWriter.store(keyVariable.type, keyVariable.local);

		statement.content.accept(statementVisitor);
	}

	public void visitAssocKeyValueIterator() {
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.MAP, "entrySet", "()Ljava/util/Set;", 0));
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.COLLECTION, "iterator", "()Ljava/util/Iterator;", 0));

		javaWriter.label(startLabel);
		javaWriter.dup();
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.ITERATOR, "hasNext", "()Z", 0));
		javaWriter.ifEQ(endLabel);
		javaWriter.dup();
		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.ITERATOR, "next", "()Ljava/lang/Object;", 0, true));
		javaWriter.checkCast(Type.getType(Map.Entry.class));
		javaWriter.dup(false);


		final JavaLocalVariableInfo keyVariable = javaWriter.getLocalVariable(statement.loopVariables[0].variable);
		final JavaLocalVariableInfo valueVariable = javaWriter.getLocalVariable(statement.loopVariables[1].variable);

		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.fromInternalName("java/util/Map$Entry", JavaClass.Kind.INTERFACE), "getKey", "()Ljava/lang/Object;", 0, true));
		this.downCast(0, keyVariable.type);
		javaWriter.store(keyVariable.type, keyVariable.local);

		javaWriter.invokeInterface(JavaMethod.getVirtual(JavaClass.fromInternalName("java/util/Map$Entry", JavaClass.Kind.INTERFACE), "getValue", "()Ljava/lang/Object;", 0, true));
		this.downCast(1, valueVariable.type);
		javaWriter.store(valueVariable.type, valueVariable.local);

		statement.content.accept(statementVisitor);
	}

	private void downCast(int typeNumber, Type t) {
		TypeID type = statement.loopVariables[typeNumber].type;
		if (CompilerUtils.isPrimitive(type)) {
			javaWriter.checkCast(statementVisitor.context.getInternalName(new OptionalTypeID(null, type)));
			type.accept(type, unboxingTypeVisitor);
		} else {
			javaWriter.checkCast(t);
		}
	}
}
