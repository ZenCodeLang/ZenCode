/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource.prepare;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.NativeTag;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.javasource.tags.JavaSourceClass;
import org.openzen.zenscript.javasource.JavaSourceFile;
import org.openzen.zenscript.javasource.tags.JavaSourceVariantOption;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourcePrepareDefinitionVisitor implements DefinitionVisitor<Void> {
	private final JavaSourceFile file;
	
	public JavaSourcePrepareDefinitionVisitor(JavaSourceFile file) {
		this.file = file;
	}
	
	@Override
	public Void visitClass(ClassDefinition definition) {
		visitClassCompiled(definition);
		return null;
	}

	@Override
	public Void visitInterface(InterfaceDefinition definition) {
		visitClassCompiled(definition);
		return null;
	}

	@Override
	public Void visitEnum(EnumDefinition definition) {
		visitClassCompiled(definition);
		return null;
	}

	@Override
	public Void visitStruct(StructDefinition definition) {
		visitClassCompiled(definition);
		return null;
	}

	@Override
	public Void visitFunction(FunctionDefinition definition) {
		return null;
	}

	@Override
	public Void visitExpansion(ExpansionDefinition definition) {
		JavaSourceClass cls = new JavaSourceClass(file.getName(), definition.pkg.fullName + "." + file.getName());
		definition.setTag(JavaSourceClass.class, cls);
		JavaSourcePrepareExpansionMethodVisitor methodVisitor = new JavaSourcePrepareExpansionMethodVisitor(cls);
		for (IDefinitionMember member : definition.members) {
			member.accept(methodVisitor);
		}
		return null;
	}

	@Override
	public Void visitAlias(AliasDefinition definition) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Void visitVariant(VariantDefinition variant) {
		JavaSourceClass cls = new JavaSourceClass(variant.name, variant.pkg.fullName + "." + variant.name);
		variant.setTag(JavaSourceClass.class, cls);
		
		for (VariantDefinition.Option option : variant.options) {
			JavaSourceClass variantCls = new JavaSourceClass(option.name, cls.fullName + "." + option.name);
			option.setTag(JavaSourceVariantOption.class, new JavaSourceVariantOption(cls, variantCls));
		}
		
		visitClassMembers(variant, cls);
		return null;
	}
	
	private void visitClassCompiled(HighLevelDefinition definition) {
		NativeTag nativeTag = definition.getTag(NativeTag.class);
		if (nativeTag == null) {
			JavaSourceClass cls = new JavaSourceClass(definition.name, definition.pkg.fullName + "." + definition.name);
			definition.setTag(JavaSourceClass.class, cls);
			visitClassMembers(definition, cls);
		} else {
			JavaSourceClass cls = getNativeClass(nativeTag.value);
			definition.setTag(JavaSourceClass.class, cls);
			visitClassMembers(definition, cls);
		}
	}
	
	private JavaSourceClass getNativeClass(String name) {
		if (name.equals("stdlib::StringBuilder"))
			return new JavaSourceClass("StringBuilder", "java.lang.StringBuilder");
		else if (name.equals("stdlib::List"))
			return new JavaSourceClass("List", "java.util.List");
		
		throw new UnsupportedOperationException("Unknown native class: " + name);
	}
	
	private void visitClassMembers(HighLevelDefinition definition, JavaSourceClass cls) {
		JavaSourcePrepareClassMethodVisitor methodVisitor = new JavaSourcePrepareClassMethodVisitor(cls);
		for (IDefinitionMember member : definition.members) {
			member.accept(methodVisitor);
		}
	}
}
