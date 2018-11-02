/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaCompileSpace;
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
	public final JavaSourceSyntheticHelperGenerator helperGenerator;
	
	public JavaSourceContext(JavaSourceModule helpers, JavaSourceFormattingSettings settings, JavaCompileSpace space, ZSPackage modulePackage, String basePackage) {
		super(space, modulePackage, basePackage);
		
		typeDescriptorVisitor = new JavaTypeDescriptorVisitor(this);
		this.generator = new JavaSourceSyntheticTypeGenerator(helpers, settings, this);
		helperGenerator = new JavaSourceSyntheticHelperGenerator(helpers, this, settings);
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
