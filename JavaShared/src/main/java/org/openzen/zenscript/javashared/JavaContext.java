/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class JavaContext {
	public abstract String getDescriptor(ITypeID type);
	
	public String getMethodDescriptor(FunctionHeader header) {
		return getMethodDescriptor(header, false);
	}
	
    public String getMethodSignature(FunctionHeader header) {
        StringBuilder signatureBuilder = new StringBuilder("(");
        for (FunctionParameter parameter : header.parameters) {
            signatureBuilder.append(getDescriptor(parameter.type));
        }
        signatureBuilder.append(")").append(getDescriptor(header.getReturnType()));
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
        descBuilder.append(getDescriptor(header.getReturnType()));
        return descBuilder.toString();
    }
}
