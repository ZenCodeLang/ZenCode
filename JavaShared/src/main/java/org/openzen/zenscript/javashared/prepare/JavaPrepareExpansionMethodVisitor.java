/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared.prepare;

import org.openzen.zenscript.codemodel.annotations.NativeTag;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.javashared.compiling.JavaCompilingClass;

/**
 * @author Hoofdgebruiker
 */
public class JavaPrepareExpansionMethodVisitor implements MemberVisitor<Void> {
	private final JavaCompilingClass class_;

	public JavaPrepareExpansionMethodVisitor(JavaCompilingClass class_) {
		this.class_ = class_;
		class_.empty = true;
	}

	@Override
	public Void visitField(FieldMember member) {
		class_.addField(member, member.getTag(NativeTag.class));
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		class_.addConstructor(member, member.getTag(NativeTag.class));
		return null;
	}

	@Override
	public Void visitMethod(MethodMember member) {
		class_.addMethod(member, member.getTag(NativeTag.class));
		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		class_.addMethod(member, member.getTag(NativeTag.class));
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		class_.addMethod(member, member.getTag(NativeTag.class));
		return null;
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		class_.addMethod(member, member.getTag(NativeTag.class));
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		class_.addMethod(member, member.getTag(NativeTag.class));
		return null;
	}

	@Override
	public Void visitCustomIterator(IteratorMember member) {
		class_.addMethod(member, member.getTag(NativeTag.class));
		return null;
	}

	@Override
	public Void visitImplementation(ImplementationMember member) {
		JavaClass implementationClass = new JavaClass(
				class_.compiled,
				JavaTypeNameVisitor.INSTANCE.process(member.type) + "Implementation",
				JavaClass.Kind.CLASS);
		class_.module.module.setImplementationInfo(member, new JavaImplementation(false, implementationClass));
		for (IDefinitionMember implementedMember : member.members)
			implementedMember.accept(this);

		return null;
	}

	@Override
	public Void visitInnerDefinition(InnerDefinitionMember member) {
		// TODO
		class_.empty = false;
		return null;
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		class_.empty = false;
		return null;
	}
}
