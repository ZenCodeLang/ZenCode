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
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javashared.*;

/**
 * @author Hoofdgebruiker
 */
public class JavaBytecodeContext extends JavaContext {
	public final JavaBytecodeModule target;
	private final TypeGenerator typeGenerator;
	private final JavaTypeInternalNameVisitor internalNameVisitor;
	private final JavaTypeDescriptorVisitor descriptorVisitor;
	private int lambdaCounter = 0;

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
		String signature = "<" + new JavaTypeGenericVisitor(this).getGenericSignature(function.typeParameters) + ">Ljava/lang/Object;";
		ClassWriter ifaceWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		ifaceWriter.visitAnnotation("Ljava/lang/FunctionalInterface;", true).visitEnd();
		ifaceWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT, function.cls.internalName, signature, "java/lang/Object", null);

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

		JavaMethod method = JavaMethod.getConstructor(range.cls, "(" + getDescriptor(range.baseType) + getDescriptor(range.baseType) + ")V", Opcodes.ACC_PUBLIC);
		JavaWriter constructorWriter = new JavaWriter(logger, CodePosition.GENERATED, rangeWriter, method, null, method.descriptor, null);
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
