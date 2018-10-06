/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javashared.JavaContext;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaSynthesizedFunction;
import org.openzen.zenscript.javashared.JavaSynthesizedRange;
import org.openzen.zenscript.javashared.JavaSyntheticClassGenerator;
import org.openzen.zenscript.javashared.JavaTypeDescriptorVisitor;
import org.openzen.zenscript.javashared.JavaTypeInternalNameVisitor;
import org.openzen.zenscript.javashared.JavaTypeUtils;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaBytecodeContext extends JavaContext {
	private final JavaModule module;
	private final TypeGenerator typeGenerator;
	private final JavaTypeInternalNameVisitor internalNameVisitor;
	private final JavaTypeDescriptorVisitor descriptorVisitor;
	private int lambdaCounter = 0;
	
	public JavaBytecodeContext(GlobalTypeRegistry registry, JavaModule module) {
		super(registry);
		
		this.module = module;
		
		typeGenerator = new TypeGenerator();
		internalNameVisitor = new JavaTypeInternalNameVisitor(this);
		descriptorVisitor = new JavaTypeDescriptorVisitor(this);
	}
	
	@Override
	protected JavaSyntheticClassGenerator getTypeGenerator() {
		return typeGenerator;
	}
	
	@Override
	public String getDescriptor(StoredType type) {
		if (JavaTypeUtils.isShared(type))
			return "Lzsynthetic/Shared";
		
		return type.type.accept(descriptorVisitor);
	}
	
	@Override
	public String getDescriptor(TypeID type) {
		return type.accept(descriptorVisitor);
	}
	
	public String getInternalName(StoredType type) {
		return type.type.accept(type, internalNameVisitor);
	}
	
	public String getInternalName(TypeID type) {
		return type.accept(null, internalNameVisitor);
	}
	
	public Type getType(StoredType type) {
		return Type.getType(getDescriptor(type));
	}
	
	public void register(String name, byte[] bytecode) {
		module.register(name, bytecode);
	}

    private void createLambdaInterface(JavaSynthesizedFunction function) {
        ClassWriter ifaceWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ifaceWriter.visitAnnotation("java/lang/FunctionalInterface", true).visitEnd();
        ifaceWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT, function.cls.internalName, null, "java/lang/Object", null);

        ifaceWriter
				.visitMethod(
					Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
					"accept",
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
		JavaWriter constructorWriter = new JavaWriter(rangeWriter, method, null, method.descriptor, null);
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

    public String getLambdaCounter() {
        return "lambda" + ++lambdaCounter;
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
		
		@Override
		public void synthesizeShared() {
			createSharedClass();
		}
	}
}
