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
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.javashared.compiling.JavaCompilingClass;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;

/**
 * @author Hoofdgebruiker
 */
public class JavaPrepareDefinitionMemberVisitor implements DefinitionVisitor<Void> {
	private final JavaCompilingClass class_;

	public JavaPrepareDefinitionMemberVisitor(JavaCompilingClass class_) {
		this.class_ = class_;
	}

	@Override
	public Void visitClass(ClassDefinition definition) {
		if (!class_.membersPrepared)
			visitClassCompiled(definition, true, JavaClass.Kind.CLASS);

		return null;
	}

	@Override
	public Void visitInterface(InterfaceDefinition definition) {
		if (class_.membersPrepared)
			return null;

		visitClassCompiled(definition, true, JavaClass.Kind.INTERFACE);
		return null;
	}

	@Override
	public Void visitEnum(EnumDefinition definition) {
		if (class_.membersPrepared)
			return null;

		visitClassCompiled(definition, false, JavaClass.Kind.ENUM);
		return null;
	}

	@Override
	public Void visitStruct(StructDefinition definition) {
		visitClassCompiled(definition, true, JavaClass.Kind.CLASS);
		return null;
	}

	@Override
	public Void visitFunction(FunctionDefinition definition) {
		JavaNativeMethod method = JavaNativeMethod.getStatic(class_.compiled, definition.name, class_.getContext().getMethodDescriptor(definition.header), JavaModifiers.getJavaModifiers(definition.modifiers));
		class_.addMethod(definition.caller, new JavaCompilingMethod(class_.compiled, method));
		return null;
	}

	@Override
	public Void visitExpansion(ExpansionDefinition definition) {
		JavaNativeClass nativeClass = class_.getContext().getJavaNativeClass(definition);
		JavaClass cls = class_.getContext().getJavaExpansionClass(definition);
		visitExpansionMembers(definition, cls, nativeClass);
		return null;
	}

	@Override
	public Void visitAlias(AliasDefinition definition) {
		// nothing to do
		return null;
	}

	@Override
	public Void visitVariant(VariantDefinition variant) {
		visitClassMembers(variant, false);
		return null;
	}

	private void visitClassCompiled(HighLevelDefinition definition, boolean startsEmpty, JavaClass.Kind kind) {
		for (TypeParameter typeParameter : definition.typeParameters) {
			class_.module.module.setTypeParameterInfo(typeParameter, new JavaTypeParameterInfo(-1));
		}

		JavaNativeClass nativeClass = class_.getContext().getJavaNativeClass(definition);
		JavaClass cls = class_.getContext().getJavaClass(definition);
		if (nativeClass == null) {
			visitClassMembers(definition, startsEmpty);
		} else {
			cls.membersPrepared = true;
			JavaClass expansionCls = class_.getContext().getJavaExpansionClass(definition);
			visitExpansionMembers(definition, expansionCls, nativeClass);
			cls.empty = expansionCls.empty;
		}
	}

	private void visitClassMembers(HighLevelDefinition definition, boolean startsEmpty) {
		class_.getContext().logger.trace("Preparing " + class_.compiled.internalName);
		JavaPrepareClassMethodVisitor methodVisitor = new JavaPrepareClassMethodVisitor(class_, this, startsEmpty);
		for (IDefinitionMember member : definition.members) {
			member.accept(methodVisitor);
		}
		class_.membersPrepared = true;
	}

	private void visitExpansionMembers(HighLevelDefinition definition, JavaClass cls, JavaNativeClass nativeClass) {
		class_.getContext().logger.trace("Preparing " + cls.internalName);
		JavaPrepareExpansionMethodVisitor methodVisitor = new JavaPrepareExpansionMethodVisitor(class_);
		for (IDefinitionMember member : definition.members) {
			member.accept(methodVisitor);
		}
		cls.membersPrepared = true;
	}
}
