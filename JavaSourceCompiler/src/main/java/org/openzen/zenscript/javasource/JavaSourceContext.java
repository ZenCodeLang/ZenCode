/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.javashared.JavaContext;
import org.openzen.zenscript.javashared.JavaSyntheticClassGenerator;
import org.openzen.zenscript.javashared.JavaTypeDescriptorVisitor;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceContext extends JavaContext {
	private final JavaTypeDescriptorVisitor typeDescriptorVisitor;
	
	public JavaSourceContext(JavaSyntheticClassGenerator generator) {
		typeDescriptorVisitor = new JavaTypeDescriptorVisitor(generator);
	}
	
	@Override
	public String getDescriptor(ITypeID type) {
		return type.accept(typeDescriptorVisitor);
	}
}
