/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared.prepare;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.*;

/**
 * @author Hoofdgebruiker
 */
public class JavaPrepareDefinitionMemberVisitor implements DefinitionVisitor<JavaClass> {
	private final JavaContext context;
	private final JavaCompiledModule module;

	public JavaPrepareDefinitionMemberVisitor(JavaContext context, JavaCompiledModule module) {
		this.context = context;
		this.module = module;
	}

	private boolean isPrepared(HighLevelDefinition definition) {
		return context.getJavaClass(definition).membersPrepared;
	}

	public void prepare(TypeID type) {
		if (!(type instanceof DefinitionTypeID))
			return;

		HighLevelDefinition definition = ((DefinitionTypeID) type).definition;
		prepare(definition);
	}

	public void prepare(HighLevelDefinition definition) {
		if (isPrepared(definition))
			return;
		if (definition.module != module.module)
			throw new IllegalArgumentException("Definition is not in the same module as the current module!");

		context.logger.trace("~~ Preparing " + definition.name);
		definition.accept(this);
	}

	@Override
	public JavaClass visitClass(ClassDefinition definition) {
		if (isPrepared(definition))
			return context.getJavaClass(definition);

		return visitClassCompiled(definition, true, JavaClass.Kind.CLASS);
	}

	@Override
	public JavaClass visitInterface(InterfaceDefinition definition) {
		if (isPrepared(definition))
			return context.getJavaClass(definition);

		for (TypeID baseType : definition.baseInterfaces)
			prepare(baseType);

		return visitClassCompiled(definition, true, JavaClass.Kind.INTERFACE);
	}

	@Override
	public JavaClass visitEnum(EnumDefinition definition) {
		if (isPrepared(definition))
			return context.getJavaClass(definition);

		return visitClassCompiled(definition, false, JavaClass.Kind.ENUM);
	}

	@Override
	public JavaClass visitStruct(StructDefinition definition) {
		if (isPrepared(definition))
			return context.getJavaClass(definition);

		return visitClassCompiled(definition, true, JavaClass.Kind.CLASS);
	}

	@Override
	public JavaClass visitFunction(FunctionDefinition definition) {
		if (isPrepared(definition))
			return context.getJavaClass(definition);

		JavaClass cls = context.getJavaClass(definition);
		JavaMethod method = JavaMethod.getStatic(cls, definition.name, context.getMethodDescriptor(definition.header), JavaModifiers.getJavaModifiers(definition.modifiers));
		module.setMethodInfo(definition.caller, method);
		return cls;
	}

	@Override
	public JavaClass visitExpansion(ExpansionDefinition definition) {
		if (isPrepared(definition))
			return context.getJavaClass(definition);

		JavaNativeClass nativeClass = context.getJavaNativeClass(definition);
		JavaClass cls = context.getJavaExpansionClass(definition);
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
		JavaClass cls = context.getJavaClass(variant);
		if (cls.membersPrepared)
			return cls;

		visitClassMembers(variant, cls, null, false);
		return cls;
	}

	private JavaClass visitClassCompiled(HighLevelDefinition definition, boolean startsEmpty, JavaClass.Kind kind) {

		for (TypeParameter typeParameter : definition.typeParameters) {
			module.setTypeParameterInfo(typeParameter, new JavaTypeParameterInfo(-1));
		}

		if (definition.getSuperType() != null)
			prepare(definition.getSuperType());

		JavaNativeClass nativeClass = context.getJavaNativeClass(definition);
		JavaClass cls = context.getJavaClass(definition);
		if (nativeClass == null) {
			visitClassMembers(definition, cls, null, startsEmpty);
		} else {
			cls.membersPrepared = true;
			JavaClass expansionCls = context.getJavaExpansionClass(definition);
			visitExpansionMembers(definition, expansionCls, nativeClass);
			cls.empty = expansionCls.empty;
		}
		return cls;
	}

	private void visitClassMembers(HighLevelDefinition definition, JavaClass cls, JavaNativeClass nativeClass, boolean startsEmpty) {
		context.logger.trace("Preparing " + cls.internalName);
		JavaPrepareClassMethodVisitor methodVisitor = new JavaPrepareClassMethodVisitor(context, module, cls, nativeClass, this, startsEmpty);
		for (IDefinitionMember member : definition.members) {
			member.accept(methodVisitor);
		}
		cls.membersPrepared = true;
	}

	private void visitExpansionMembers(HighLevelDefinition definition, JavaClass cls, JavaNativeClass nativeClass) {
		context.logger.trace("Preparing " + cls.internalName);
		JavaPrepareExpansionMethodVisitor methodVisitor = new JavaPrepareExpansionMethodVisitor(context, module, cls, nativeClass);
		for (IDefinitionMember member : definition.members) {
			member.accept(methodVisitor);
		}
		cls.membersPrepared = true;
	}
}
