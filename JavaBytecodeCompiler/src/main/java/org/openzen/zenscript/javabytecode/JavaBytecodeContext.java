/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hoofdgebruiker
 */
public class JavaBytecodeContext extends JavaContext {
	public final JavaBytecodeModule target;
	private final TypeGenerator typeGenerator;
	private final JavaTypeInternalNameVisitor internalNameVisitor;
	private final JavaTypeDescriptorVisitor descriptorVisitor;
	private int lambdaCounter = 0;
	private final Map<LoopStatement.ObjectId, BytecodeLoopLabels> bytecodeLoopLabels = new HashMap<>();

	public JavaBytecodeContext(JavaBytecodeModule target, JavaCompileSpace space, ZSPackage modulePackage, String basePackage, IZSLogger logger) {
		super(space, modulePackage, basePackage, logger);

		this.target = target;

		typeGenerator = new TypeGenerator();
		internalNameVisitor = new JavaTypeInternalNameVisitor(this);
		descriptorVisitor = new JavaTypeDescriptorVisitor(this);
	}

	@Override
	protected JavaSyntheticClassGenerator getTypeGenerator() {
		return typeGenerator;
	}

	@Override
	public String getDescriptor(TypeID type) {
		return type.accept(descriptorVisitor);
	}

	public String getInternalName(TypeID type) {
		return type.accept(internalNameVisitor);
	}

	public Type getType(TypeID type) {
		return Type.getType(getDescriptor(type));
	}

	public void register(String name, byte[] bytecode) {
		target.addClass(name, bytecode);
	}

	private void createLambdaInterface(JavaSynthesizedFunction function) {
		ClassWriter ifaceWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		ifaceWriter.visitAnnotation("java/lang/FunctionalInterface", true).visitEnd();
		ifaceWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT, function.cls.internalName, null, "java/lang/Object", null);

		ifaceWriter
				.visitMethod(
						Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
						function.method,
						getMethodDescriptor(function.header),
						getMethodSignature(function.header),
						null)
				.visitEnd();

		register(function.cls.internalName, ifaceWriter.toByteArray());
	}

	private void createRangeClass(JavaSynthesizedRange range) {
		ClassWriter rangeWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		rangeWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, range.cls.internalName, null, "java/lang/Object", null);
		rangeWriter.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "from", getDescriptor(range.baseType), null, null).visitEnd();
		rangeWriter.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "to", getDescriptor(range.baseType), null, null).visitEnd();

		final FunctionHeader ctorHeader = new FunctionHeader(BasicTypeID.VOID,
				new FunctionParameter(range.baseType, "from"),
				new FunctionParameter(range.baseType, "to")
		);
		JavaNativeMethod method = JavaNativeMethod.getConstructor(range.cls, getMethodDescriptor(ctorHeader), Opcodes.ACC_PUBLIC);
		JavaCompilingMethod compilingMethod = new JavaCompilingMethod(range.compiling.compiled, method, getMethodSignature(ctorHeader));

		JavaWriter constructorWriter = new JavaWriter(logger, CodePosition.GENERATED, rangeWriter, compilingMethod, null);
		constructorWriter.loadObject(0);
		constructorWriter.invokeSpecial("java/lang/Object", "<init>", "()V");
		constructorWriter.loadObject(0);
		constructorWriter.load(getType(range.baseType), 1);
		constructorWriter.putField(range.cls.internalName, "from", getDescriptor(range.baseType));
		constructorWriter.loadObject(0);
		constructorWriter.load(getType(range.baseType), 2);
		constructorWriter.putField(range.cls.internalName, "to", getDescriptor(range.baseType));
		constructorWriter.ret();
		constructorWriter.end();

		rangeWriter.visitEnd();

		register(range.cls.internalName, rangeWriter.toByteArray());
	}

	private void createSharedClass() {
		// TODO
	}

	public int getLambdaCounter() {
		return ++lambdaCounter;
	}

	public BytecodeLoopLabels getLoopLabels(LoopStatement loopStatement) {
		return bytecodeLoopLabels.get(loopStatement.objectId);
	}

	public void setLoopLabels(LoopStatement loopStatement, BytecodeLoopLabels bytecodeLoopLabels) {
		this.bytecodeLoopLabels.put(loopStatement.objectId, bytecodeLoopLabels);
	}

    private class TypeGenerator implements JavaSyntheticClassGenerator {

		@Override
		public void synthesizeFunction(JavaSynthesizedFunction function) {
			createLambdaInterface(function);
		}

		@Override
		public void synthesizeRange(JavaSynthesizedRange range) {
			createRangeClass(range);
		}
	}
}
