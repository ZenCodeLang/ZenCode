/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.javabytecode.compiler.CompilerUtils;
import org.openzen.zenscript.javashared.JavaSynthesizedClass;
import org.openzen.zenscript.javashared.JavaSyntheticClassGenerator;
import org.openzen.zenscript.javashared.JavaTypeDescriptorVisitor;
import org.openzen.zenscript.javashared.JavaTypeInternalNameVisitor;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaContext {
	private final JavaModule module;
	private final TypeGenerator typeGenerator;
	private final JavaTypeInternalNameVisitor internalNameVisitor;
	private final JavaTypeDescriptorVisitor descriptorVisitor;
	
	public JavaContext(JavaModule module) {
		this.module = module;
		
		typeGenerator = new TypeGenerator();
		internalNameVisitor = new JavaTypeInternalNameVisitor(typeGenerator);
		descriptorVisitor = new JavaTypeDescriptorVisitor(typeGenerator);
	}
	
	public JavaSyntheticClassGenerator getTypeGenerator() {
		return typeGenerator;
	}
	
	public String getDescriptor(ITypeID type) {
		return type.accept(descriptorVisitor);
	}
	
	public String getInternalName(ITypeID type) {
		return type.accept(internalNameVisitor);
	}
	
	public Type getType(ITypeID type) {
		return Type.getType(getDescriptor(type));
	}
	
	public String getMethodDescriptor(FunctionHeader header) {
		return getMethodDescriptor(header, false);
	}
	
    public String getMethodSignature(FunctionHeader header) {
        StringBuilder signatureBuilder = new StringBuilder("(");
        for (FunctionParameter parameter : header.parameters) {
            signatureBuilder.append(getDescriptor(parameter.type));
        }
        signatureBuilder.append(")").append(getDescriptor(header.returnType));
        return signatureBuilder.toString();
    }
	
	public String getEnumConstructorDescriptor(FunctionHeader header) {
		return getMethodDescriptor(header, true);
	}
	
	private String getMethodDescriptor(FunctionHeader header, boolean isEnumConstructor) {
        StringBuilder descBuilder = new StringBuilder("(");
        if (isEnumConstructor)
            descBuilder.append("Ljava/lang/String;I");
		
        for (FunctionParameter parameter : header.parameters) {
			descBuilder.append(getDescriptor(parameter.type));
        }
        descBuilder.append(")");
        descBuilder.append(getDescriptor(header.returnType));
        return descBuilder.toString();
    }
	
	public void register(String name, byte[] bytecode) {
		module.register(name, bytecode);
	}
	
	private class TypeGenerator implements JavaSyntheticClassGenerator {

		@Override
		public JavaSynthesizedClass synthesizeFunction(FunctionTypeID type) {
			return CompilerUtils.getLambdaInterface(JavaContext.this, type);
		}

		@Override
		public JavaSynthesizedClass synthesizeRange(RangeTypeID type) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
	}
}
