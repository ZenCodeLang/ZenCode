/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaContext;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaSynthesizedClass;
import org.openzen.zenscript.javashared.JavaSynthesizedClassNamer;
import org.openzen.zenscript.javashared.JavaSyntheticClassGenerator;
import org.openzen.zenscript.javashared.JavaTypeDescriptorVisitor;
import org.openzen.zenscript.javashared.JavaTypeInternalNameVisitor;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaBytecodeContext extends JavaContext {
	private final JavaModule module;
	private final TypeGenerator typeGenerator;
	private final JavaTypeInternalNameVisitor internalNameVisitor;
	private final JavaTypeDescriptorVisitor descriptorVisitor;
	private final Map<String, JavaSynthesizedClass> functions = new HashMap<>();
	private final Map<String, JavaSynthesizedClass> ranges = new HashMap<>();
	private int lambdaCounter = 0;
	
	public JavaBytecodeContext(JavaModule module) {
		this.module = module;
		
		typeGenerator = new TypeGenerator();
		internalNameVisitor = new JavaTypeInternalNameVisitor(typeGenerator);
		descriptorVisitor = new JavaTypeDescriptorVisitor(typeGenerator);
	}
	
	public JavaSyntheticClassGenerator getTypeGenerator() {
		return typeGenerator;
	}
	
	@Override
	public String getDescriptor(ITypeID type) {
		return type.accept(descriptorVisitor);
	}
	
	public String getInternalName(ITypeID type) {
		return type.accept(internalNameVisitor);
	}
	
	public Type getType(ITypeID type) {
		return Type.getType(getDescriptor(type));
	}
	
	public void register(String name, byte[] bytecode) {
		module.register(name, bytecode);
	}

    private JavaSynthesizedClass getLambdaInterface(FunctionTypeID function) {
		String signature = JavaSynthesizedClassNamer.getFunctionSignature(function);
		if (functions.containsKey(signature))
			return functions.get(signature).withTypeParameters(JavaSynthesizedClassNamer.extractTypeParameters(function));
		
		JavaSynthesizedClass result = JavaSynthesizedClassNamer.createFunctionName(function);
		functions.put(signature, result);
		
        createLambdaInterface(function.header, result.cls);
        return result;
    }
	
	private JavaSynthesizedClass getRangeType(RangeTypeID type) {
		String signature = JavaSynthesizedClassNamer.getRangeSignature(type);
		if (ranges.containsKey(signature))
			return ranges.get(signature).withTypeParameters(JavaSynthesizedClassNamer.extractTypeParameters(type));
		
		JavaSynthesizedClass result = JavaSynthesizedClassNamer.createRangeName(type);
		ranges.put(signature, result);
		
		createRangeClass(type.baseType, result.cls);
		return result;
	}

    private void createLambdaInterface(FunctionHeader header, JavaClass cls) {
        ClassWriter ifaceWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ifaceWriter.visitAnnotation("java/lang/FunctionalInterface", true).visitEnd();
        ifaceWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT, cls.internalName, null, "java/lang/Object", null);

        ifaceWriter
				.visitMethod(
					Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
					"accept",
					getMethodDescriptor(header),
					getMethodSignature(header),
					null)
				.visitEnd();

        register(cls.internalName, ifaceWriter.toByteArray());
    }
	
	private void createRangeClass(ITypeID baseType, JavaClass cls) {
		ClassWriter rangeWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		rangeWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, cls.internalName, null, "java/lang/Object", null);
		rangeWriter.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "from", getDescriptor(baseType), null, null).visitEnd();
		rangeWriter.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "to", getDescriptor(baseType), null, null).visitEnd();
		
		JavaMethod method = JavaMethod.getConstructor(cls, "(" + getDescriptor(baseType) + getDescriptor(baseType) + ")V", Opcodes.ACC_PUBLIC);
		JavaWriter constructorWriter = new JavaWriter(rangeWriter, method, null, method.descriptor, null);
		constructorWriter.loadObject(0);
		constructorWriter.invokeSpecial("java/lang/Object", "<init>", "()V");
		constructorWriter.loadObject(0);
		constructorWriter.load(getType(baseType), 1);
		constructorWriter.putField(cls.internalName, "from", getDescriptor(baseType));
		constructorWriter.loadObject(0);
		constructorWriter.load(getType(baseType), 2);
		constructorWriter.putField(cls.internalName, "to", getDescriptor(baseType));
		constructorWriter.ret();
		constructorWriter.end();
		
		rangeWriter.visitEnd();
		
		register(cls.internalName, rangeWriter.toByteArray());
	}

    public String getLambdaCounter() {
        return "lambda" + ++lambdaCounter;
    }
	
	private class TypeGenerator implements JavaSyntheticClassGenerator {

		@Override
		public JavaSynthesizedClass synthesizeFunction(FunctionTypeID type) {
			return getLambdaInterface(type);
		}

		@Override
		public JavaSynthesizedClass synthesizeRange(RangeTypeID type) {
			return getRangeType(type);
		}
	}
}
