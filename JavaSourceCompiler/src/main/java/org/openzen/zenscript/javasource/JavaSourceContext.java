/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.io.File;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
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
	public String getDescriptor(TypeID type) {
		return typeDescriptorVisitor.process(type);
	}
	
	@Override
	public String getDescriptor(StoredType type) {
		return typeDescriptorVisitor.process(type);
	}

	@Override
	protected JavaSyntheticClassGenerator getTypeGenerator() {
		return generator;
	}
}
