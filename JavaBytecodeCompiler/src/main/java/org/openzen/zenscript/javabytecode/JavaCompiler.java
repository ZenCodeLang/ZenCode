/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.member.BuiltinTypeMembers;
import org.openzen.zenscript.javabytecode.compiler.JavaStatementVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.shared.SourceFile;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaCompiler {
	static {
		JavaClassInfo jInteger = new JavaClassInfo("java/lang/Integer");
		BuiltinTypeMembers.INT_GET_MIN_VALUE.setTag(JavaFieldInfo.class, new JavaFieldInfo(jInteger, "MIN_VALUE", "I"));
		BuiltinTypeMembers.INT_GET_MAX_VALUE.setTag(JavaFieldInfo.class, new JavaFieldInfo(jInteger, "MAX_VALUE", "I"));
		
		BuiltinTypeMembers.INT_ADD_INT.setTag(JavaBytecodeImplementation.class, writer -> writer.iAdd());
		BuiltinTypeMembers.INT_SUB_INT.setTag(JavaBytecodeImplementation.class, writer -> writer.iSub());
		BuiltinTypeMembers.INT_MUL_INT.setTag(JavaBytecodeImplementation.class, writer -> writer.iMul());
		BuiltinTypeMembers.INT_DIV_INT.setTag(JavaBytecodeImplementation.class, writer -> writer.iDiv());
		BuiltinTypeMembers.INT_MOD_INT.setTag(JavaBytecodeImplementation.class, writer -> writer.iRem());
		
		BuiltinTypeMembers.INT_TO_BYTE.setTag(JavaBytecodeImplementation.class, writer -> writer.i2b());
		BuiltinTypeMembers.INT_TO_SBYTE.setTag(JavaBytecodeImplementation.class, writer -> writer.i2b());
		BuiltinTypeMembers.INT_TO_SHORT.setTag(JavaBytecodeImplementation.class, writer -> writer.i2s());
		BuiltinTypeMembers.INT_TO_USHORT.setTag(JavaBytecodeImplementation.class, writer -> writer.i2s());
		BuiltinTypeMembers.INT_TO_UINT.setTag(JavaBytecodeImplementation.class, writer -> {});
		BuiltinTypeMembers.INT_TO_LONG.setTag(JavaBytecodeImplementation.class, writer -> writer.i2l());
		BuiltinTypeMembers.INT_TO_ULONG.setTag(JavaBytecodeImplementation.class, writer -> writer.i2l());
		BuiltinTypeMembers.INT_TO_FLOAT.setTag(JavaBytecodeImplementation.class, writer -> writer.i2f());
		BuiltinTypeMembers.INT_TO_DOUBLE.setTag(JavaBytecodeImplementation.class, writer -> writer.i2d());
		BuiltinTypeMembers.INT_TO_CHAR.setTag(JavaBytecodeImplementation.class, writer -> writer.i2s());
		BuiltinTypeMembers.INT_TO_STRING.setTag(JavaMethodInfo.class, new JavaMethodInfo(jInteger, "toString", "(I)Ljava/lang/String;", true));
	}
	
	private final JavaModule target;
	private final List<String> scriptBlockNames = new ArrayList<>();
	private final ClassWriter scriptsClassWriter;
	private int generatedScriptBlockCounter = 0;
	private boolean finished = false;
	
	public JavaCompiler() {
		this(false);
	}
	
	public JavaCompiler(boolean debug) {
		target = new JavaModule();
		
		scriptsClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		scriptsClassWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Scripts", null, "java/lang/Object", null);
	}
	
	public void addDefinition(HighLevelDefinition definition) {
		// convert definition into java class
	}
	
	public void addScriptBlock(ScriptBlock script) {
		SourceFile sourceFile = script.getTag(SourceFile.class);
		String methodName;
		if (sourceFile == null) {
			methodName = "generatedBlock" + (generatedScriptBlockCounter++);
		} else {
			// TODO: remove special characters
			methodName = sourceFile.filename.substring(0, sourceFile.filename.lastIndexOf('.')).replace("/", "_");
		}

		scriptBlockNames.add(methodName);

		// convert scripts into methods (add them to a Scripts class?)
		// (TODO: can we break very long scripts into smaller methods? for the extreme scripts)
		final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(new JavaWriter(scriptsClassWriter, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, methodName, "()V", null, null));
		statementVisitor.start();
		for (Statement statement : script.statements) {
			statement.accept(statementVisitor);
		}
		statementVisitor.end();
	}
	
	public JavaModule finish() {
		if (finished)
			throw new IllegalStateException("Already finished!");
		finished = true;
		
		final JavaWriter runWriter = new JavaWriter(scriptsClassWriter, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "run", "()V", null, null);
		runWriter.start();
		for (String scriptBlockName : scriptBlockNames) {
			runWriter.invokeStatic("Scripts", scriptBlockName, "()V");
		}
		runWriter.ret();
		runWriter.end();
		
		target.register("Scripts", scriptsClassWriter.toByteArray());
		
		return target;
	}
}
