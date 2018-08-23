/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared.prepare;

import org.openzen.zenscript.javashared.JavaNativeClass;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
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
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaContext;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaModifiers;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaPrepareDefinitionMemberVisitor implements DefinitionVisitor<JavaClass> {
	private final JavaContext context;
	
	public JavaPrepareDefinitionMemberVisitor(JavaContext context) {
		this.context = context;
	}
	
	private boolean isPrepared(HighLevelDefinition definition) {
		return definition.getTag(JavaClass.class).membersPrepared;
	}
	
	public void prepare(ITypeID type) {
		if (!(type instanceof DefinitionTypeID))
			return;
			
		HighLevelDefinition definition = ((DefinitionTypeID)type).definition;
		definition.accept(this);
	}
	
	@Override
	public JavaClass visitClass(ClassDefinition definition) {
		if (isPrepared(definition))
			return definition.getTag(JavaClass.class);
		
		return visitClassCompiled(definition, true, JavaClass.Kind.CLASS);
	}

	@Override
	public JavaClass visitInterface(InterfaceDefinition definition) {
		if (isPrepared(definition))
			return definition.getTag(JavaClass.class);
		
		for (ITypeID baseType : definition.baseInterfaces)
			prepare(baseType);
		
		return visitClassCompiled(definition, true, JavaClass.Kind.INTERFACE);
	}

	@Override
	public JavaClass visitEnum(EnumDefinition definition) {
		if (isPrepared(definition))
			return definition.getTag(JavaClass.class);
		
		return visitClassCompiled(definition, false, JavaClass.Kind.ENUM);
	}

	@Override
	public JavaClass visitStruct(StructDefinition definition) {
		if (isPrepared(definition))
			return definition.getTag(JavaClass.class);
		
		return visitClassCompiled(definition, true, JavaClass.Kind.CLASS);
	}

	@Override
	public JavaClass visitFunction(FunctionDefinition definition) {
		if (isPrepared(definition))
			return definition.getTag(JavaClass.class);
		
		JavaClass cls = definition.getTag(JavaClass.class);
		JavaMethod method = JavaMethod.getStatic(cls, definition.name, context.getMethodDescriptor(definition.header), JavaModifiers.getJavaModifiers(definition.modifiers));
		definition.caller.setTag(JavaMethod.class, method);
		return cls;
	}

	@Override
	public JavaClass visitExpansion(ExpansionDefinition definition) {
		if (isPrepared(definition))
			return definition.getTag(JavaClass.class);
		
		JavaNativeClass nativeClass = definition.getTag(JavaNativeClass.class);
		JavaClass cls = definition.getTag(JavaClass.class);
		visitExpansionMembers(definition, cls, nativeClass);
		return cls;
	}

	@Override
	public JavaClass visitAlias(AliasDefinition definition) {
		// nothing to do
		return null;
	}

	@Override
	public JavaClass visitVariant(VariantDefinition variant) {
		if (isPrepared(variant))
			return variant.getTag(JavaClass.class);
		
		JavaClass cls = variant.getTag(JavaClass.class);
		visitClassMembers(variant, cls, null, false);
		return cls;
	}
	
	private JavaClass visitClassCompiled(HighLevelDefinition definition, boolean startsEmpty, JavaClass.Kind kind) {
		if (definition.getSuperType() != null)
			prepare(definition.getSuperType());
		
		JavaNativeClass nativeClass = definition.getTag(JavaNativeClass.class);
		JavaClass cls = definition.getTag(JavaClass.class);
		if (nativeClass == null) {
			visitClassMembers(definition, cls, null, startsEmpty);
		} else {
			visitExpansionMembers(definition, cls, nativeClass);
		}
		return cls;
	}
	
	private void visitClassMembers(HighLevelDefinition definition, JavaClass cls, JavaNativeClass nativeClass, boolean startsEmpty) {
		System.out.println("Preparing " + cls.internalName);
		JavaPrepareClassMethodVisitor methodVisitor = new JavaPrepareClassMethodVisitor(context, cls, nativeClass, this, startsEmpty);
		for (IDefinitionMember member : definition.members) {
			member.accept(methodVisitor);
		}
		cls.membersPrepared = true;
	}
	
	private void visitExpansionMembers(HighLevelDefinition definition, JavaClass cls, JavaNativeClass nativeClass) {
		System.out.println("Preparing " + cls.internalName);
		JavaPrepareExpansionMethodVisitor methodVisitor = new JavaPrepareExpansionMethodVisitor(context, cls, nativeClass);
		for (IDefinitionMember member : definition.members) {
			member.accept(methodVisitor);
		}
		cls.membersPrepared = true;
	}
}
