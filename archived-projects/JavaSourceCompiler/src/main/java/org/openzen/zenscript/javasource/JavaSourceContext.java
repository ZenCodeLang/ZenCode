/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zencode.shared.logging.*;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaCompileSpace;
import org.openzen.zenscript.javashared.JavaContext;
import org.openzen.zenscript.javashared.JavaSyntheticClassGenerator;
import org.openzen.zenscript.javashared.JavaTypeDescriptorVisitor;

/**
 * @author Hoofdgebruiker
 */
public class JavaSourceContext extends JavaContext {
	public final JavaSourceSyntheticHelperGenerator helperGenerator;
	private final JavaTypeDescriptorVisitor typeDescriptorVisitor;
	private final JavaSyntheticClassGenerator generator;

	public JavaSourceContext(IZSLogger logger, JavaSourceModule helpers, JavaSourceFormattingSettings settings, JavaCompileSpace space, ZSPackage modulePackage, String basePackage) {
		super(space, modulePackage, basePackage, logger);

		typeDescriptorVisitor = new JavaTypeDescriptorVisitor(this);
		this.generator = new JavaSourceSyntheticTypeGenerator(helpers, settings, this);
		helperGenerator = new JavaSourceSyntheticHelperGenerator(helpers, this, settings);

		init();
	}

	@Override
	public String getDescriptor(TypeID type) {
		return typeDescriptorVisitor.process(type);
	}

	@Override
	protected JavaSyntheticClassGenerator getTypeGenerator() {
		return generator;
	}
}
