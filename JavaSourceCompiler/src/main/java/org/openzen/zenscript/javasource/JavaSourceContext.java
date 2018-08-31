/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.io.File;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
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
	private final JavaSyntheticClassGenerator generator;
	
	public JavaSourceContext(GlobalTypeRegistry registry, File directory, JavaSourceFormattingSettings settings) {
		super(registry);
		
		typeDescriptorVisitor = new JavaTypeDescriptorVisitor(this);
		this.generator = new JavaSourceSyntheticTypeGenerator(directory, settings, this);
	}
	
	@Override
	public String getDescriptor(ITypeID type) {
		return type.accept(typeDescriptorVisitor);
	}

	@Override
	protected JavaSyntheticClassGenerator getTypeGenerator() {
		return generator;
	}
}
