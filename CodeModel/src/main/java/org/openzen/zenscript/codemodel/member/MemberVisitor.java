package org.openzen.zenscript.codemodel.member;

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
