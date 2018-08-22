/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.javashared.JavaTypeDescriptorVisitor;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceContext {
	private final JavaTypeDescriptorVisitor typeDescriptorVisitor;
	
	public JavaSourceContext(JavaSourceSyntheticTypeGenerator generator) {
		typeDescriptorVisitor = new JavaTypeDescriptorVisitor(generator);
	}
	
	public String getDescriptor(ITypeID type) {
		return type.accept(typeDescriptorVisitor);
	}
}
