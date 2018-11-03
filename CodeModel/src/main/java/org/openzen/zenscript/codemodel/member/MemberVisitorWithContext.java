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
public interface MemberVisitorWithContext<C, R> {
	R visitConst(C context, ConstMember member);
	
	R visitField(C context, FieldMember member);
	
	R visitConstructor(C context, ConstructorMember member);
	
	R visitDestructor(C context, DestructorMember member);
	
	R visitMethod(C context, MethodMember member);
	
	R visitGetter(C context, GetterMember member);
	
	R visitSetter(C context, SetterMember member);
	
	R visitOperator(C context, OperatorMember member);
	
	R visitCaster(C context, CasterMember member);
	
	R visitIterator(C context, IteratorMember member);
	
	R visitCaller(C context, CallerMember member);
	
	R visitImplementation(C context, ImplementationMember member);
	
	R visitInnerDefinition(C context, InnerDefinitionMember member);
	
	R visitStaticInitializer(C context, StaticInitializerMember member);
}
