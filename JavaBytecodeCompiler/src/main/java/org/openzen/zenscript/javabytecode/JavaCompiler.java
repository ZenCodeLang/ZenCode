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
import org.openzen.zenscript.codemodel.member.DefinitionMember;
import org.openzen.zenscript.codemodel.statement.Statement;
import static org.openzen.zenscript.codemodel.type.member.BuiltinTypeMembers.*;

import org.openzen.zenscript.javabytecode.compiler.definitions.JavaDefinitionVisitor;
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
		JavaClassInfo jLong = new JavaClassInfo("java/lang/Long");
		JavaClassInfo jFloat = new JavaClassInfo("java/lang/Float");
		JavaClassInfo jDouble = new JavaClassInfo("java/lang/Double");
		
		INT_GET_MIN_VALUE.setTag(JavaFieldInfo.class, new JavaFieldInfo(jInteger, "MIN_VALUE", "I"));
		INT_GET_MAX_VALUE.setTag(JavaFieldInfo.class, new JavaFieldInfo(jInteger, "MAX_VALUE", "I"));
		
		LONG_GET_MIN_VALUE.setTag(JavaFieldInfo.class, new JavaFieldInfo(jLong, "MIN_VALUE", "J"));
		LONG_GET_MAX_VALUE.setTag(JavaFieldInfo.class, new JavaFieldInfo(jLong, "MAX_VALUE", "J"));
		
		implement(BOOL_NOT, JavaWriter::iNot);
		
		implement(BYTE_NOT, JavaWriter::iNot);
		implement(SBYTE_NOT, JavaWriter::iNot);
		implement(SHORT_NOT, JavaWriter::iNot);
		implement(USHORT_NOT, JavaWriter::iNot);
		implement(INT_NOT, JavaWriter::iNot);
		implement(UINT_NOT, JavaWriter::iNot);
		implement(LONG_NOT, JavaWriter::lNot);
		implement(ULONG_NOT, JavaWriter::lNot);
		
		implement(BYTE_NEG, JavaWriter::iNeg);
		implement(SBYTE_NEG, JavaWriter::iNeg);
		implement(SHORT_NEG, JavaWriter::iNeg);
		implement(USHORT_NEG, JavaWriter::iNeg);
		implement(INT_NEG, JavaWriter::iNeg);
		implement(UINT_NEG, JavaWriter::iNeg);
		implement(LONG_NEG, JavaWriter::lNeg);
		implement(ULONG_NEG, JavaWriter::lNeg);
		implement(FLOAT_NEG, JavaWriter::fNeg);
		implement(DOUBLE_NEG, JavaWriter::dNeg);
		
		implement(BYTE_ADD_BYTE, JavaWriter::iAdd);
		implement(SBYTE_ADD_SBYTE, JavaWriter::iAdd);
		implement(SHORT_ADD_SHORT, JavaWriter::iAdd);
		implement(USHORT_ADD_USHORT, JavaWriter::iAdd);
		implement(INT_ADD_INT, JavaWriter::iAdd);
		implement(UINT_ADD_UINT, JavaWriter::iAdd);
		implement(LONG_ADD_LONG, JavaWriter::lAdd);
		implement(ULONG_ADD_ULONG, JavaWriter::lAdd);
		implement(FLOAT_ADD_FLOAT, JavaWriter::fAdd);
		implement(DOUBLE_ADD_DOUBLE, JavaWriter::dAdd);
		// TODO: STRING_ADD_STRING

		implement(STRING_ADD_STRING, JavaWriter::stringAdd);
		
		implement(BYTE_SUB_BYTE, JavaWriter::iSub);
		implement(SBYTE_SUB_SBYTE, JavaWriter::iSub);
		implement(SHORT_SUB_SHORT, JavaWriter::iSub);
		implement(USHORT_SUB_USHORT, JavaWriter::iSub);
		implement(INT_SUB_INT, JavaWriter::iSub);
		implement(UINT_SUB_UINT, JavaWriter::iSub);
		implement(LONG_SUB_LONG, JavaWriter::lSub);
		implement(ULONG_SUB_ULONG, JavaWriter::lSub);
		implement(FLOAT_SUB_FLOAT, JavaWriter::fSub);
		implement(DOUBLE_SUB_DOUBLE, JavaWriter::dSub);
		
		implement(BYTE_MUL_BYTE, JavaWriter::iMul);
		implement(SBYTE_MUL_SBYTE, JavaWriter::iMul);
		implement(SHORT_MUL_SHORT, JavaWriter::iMul);
		implement(USHORT_MUL_USHORT, JavaWriter::iMul);
		implement(INT_MUL_INT, JavaWriter::iMul);
		implement(UINT_MUL_UINT, JavaWriter::iMul);
		implement(LONG_MUL_LONG, JavaWriter::lMul);
		implement(ULONG_MUL_ULONG, JavaWriter::lMul);
		implement(FLOAT_MUL_FLOAT, JavaWriter::fMul);
		implement(DOUBLE_MUL_DOUBLE, JavaWriter::dMul);
		
		implement(INT_DIV_INT, JavaWriter::iDiv);
		implement(INT_MOD_INT, JavaWriter::iRem);
		
		implement(INT_TO_BYTE, JavaWriter::i2b);
		implement(INT_TO_SBYTE, JavaWriter::i2b);
		implement(INT_TO_SHORT, JavaWriter::i2s);
		implement(INT_TO_USHORT, JavaWriter::i2s);
		implement(INT_TO_UINT, writer -> {});
		implement(INT_TO_LONG, JavaWriter::i2l);
		implement(INT_TO_ULONG, JavaWriter::i2l);
		implement(INT_TO_FLOAT, JavaWriter::i2f);
		implement(INT_TO_DOUBLE, JavaWriter::i2d);
		implement(INT_TO_CHAR, JavaWriter::i2s);
		INT_TO_STRING.setTag(JavaMethodInfo.class, new JavaMethodInfo(jInteger, "toString", "(I)Ljava/lang/String;", true));
		
		implement(LONG_TO_BYTE, writer -> { writer.l2i(); writer.i2b(); });
		implement(LONG_TO_SBYTE, writer -> { writer.l2i(); writer.i2b(); });
		implement(LONG_TO_SHORT, writer -> { writer.l2i(); writer.i2s(); });
		implement(LONG_TO_USHORT, writer -> { writer.l2i(); writer.i2s(); });
		implement(LONG_TO_INT, JavaWriter::l2i);
		implement(LONG_TO_UINT, JavaWriter::l2i);
		implement(LONG_TO_ULONG, writer -> {});
		implement(LONG_TO_FLOAT, JavaWriter::l2f);
		implement(LONG_TO_DOUBLE, JavaWriter::l2d);
		implement(LONG_TO_CHAR, writer -> { writer.l2i(); writer.i2s(); });
		LONG_TO_STRING.setTag(JavaMethodInfo.class, new JavaMethodInfo(jLong, "toString", "(J)Ljava/lang/String;", true));
		
		FLOAT_BITS.setTag(JavaMethodInfo.class, new JavaMethodInfo(jFloat, "floatToRawIntBits", "(F)I", true));
		DOUBLE_BITS.setTag(JavaMethodInfo.class, new JavaMethodInfo(jDouble, "doubleToRawLongBits", "(D)J", true));
		FLOAT_FROMBITS.setTag(JavaMethodInfo.class, new JavaMethodInfo(jFloat, "intBitsToFloat", "(I)F", true));
		DOUBLE_FROMBITS.setTag(JavaMethodInfo.class, new JavaMethodInfo(jDouble, "longBitsToDouble", "(J)D", true));
	}
	
	private static void implement(DefinitionMember member, JavaBytecodeImplementation implementation) {
		member.setTag(JavaBytecodeImplementation.class, implementation);
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
		target.register(definition.name, definition.accept(new JavaDefinitionVisitor()));

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
		target.register("Scripts", scriptsClassWriter.toByteArray());
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
