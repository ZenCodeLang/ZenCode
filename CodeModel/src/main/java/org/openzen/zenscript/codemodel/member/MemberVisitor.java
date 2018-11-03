/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

/**
 *
 * @author Hoofdgebruiker
 */
public interface MemberVisitor<T> {
	T visitConst(ConstMember member);
	
	T visitField(FieldMember member);
	
	T visitConstructor(ConstructorMember member);
	
	T visitDestructor(DestructorMember member);
	
	T visitMethod(MethodMember member);
	
	T visitGetter(GetterMember member);
	
	T visitSetter(SetterMember member);
	
	T visitOperator(OperatorMember member);
	
	T visitCaster(CasterMember member);
	
	T visitCustomIterator(IteratorMember member);
	
	T visitCaller(CallerMember member);
	
	T visitImplementation(ImplementationMember member);
	
	T visitInnerDefinition(InnerDefinitionMember member);
	
	T visitStaticInitializer(StaticInitializerMember member);
}
