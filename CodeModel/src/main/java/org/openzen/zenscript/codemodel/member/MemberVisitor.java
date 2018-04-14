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
	public T visitField(FieldMember member);
	
	public T visitConstructor(ConstructorMember member);
	
	public T visitMethod(MethodMember member);
	
	public T visitGetter(GetterMember member);
	
	public T visitSetter(SetterMember member);
	
	public T visitEnumConstant(EnumConstantMember member);
	
	public T visitOperator(OperatorMember member);
	
	public T visitCaster(CasterMember member);
	
	public T visitCustomIterator(CustomIteratorMember member);
	
	public T visitCaller(CallerMember member);
	
	public T visitImplementation(ImplementationMember member);
	
	public T visitInnerDefinition(InnerDefinitionMember member);
}
