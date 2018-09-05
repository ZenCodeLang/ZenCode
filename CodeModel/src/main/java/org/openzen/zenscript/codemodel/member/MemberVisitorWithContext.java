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
	public R visitConst(C context, ConstMember member);
	
	public R visitField(C context, FieldMember member);
	
	public R visitConstructor(C context, ConstructorMember member);
	
	public R visitDestructor(C context, DestructorMember member);
	
	public R visitMethod(C context, MethodMember member);
	
	public R visitGetter(C context, GetterMember member);
	
	public R visitSetter(C context, SetterMember member);
	
	public R visitOperator(C context, OperatorMember member);
	
	public R visitCaster(C context, CasterMember member);
	
	public R visitIterator(C context, IteratorMember member);
	
	public R visitCaller(C context, CallerMember member);
	
	public R visitImplementation(C context, ImplementationMember member);
	
	public R visitInnerDefinition(C context, InnerDefinitionMember member);
	
	public R visitStaticInitializer(C context, StaticInitializerMember member);
}
