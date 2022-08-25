package org.openzen.zenscript.codemodel.member;

public interface MemberVisitorWithContext<C, R> {
	R visitField(C context, FieldMember member);

	R visitConstructor(C context, ConstructorMember member);

	R visitMethod(C context, MethodMember member);

	R visitGetter(C context, GetterMember member);

	R visitSetter(C context, SetterMember member);

	R visitOperator(C context, OperatorMember member);

	R visitCaster(C context, CasterMember member);

	R visitIterator(C context, IteratorMember member);

	R visitImplementation(C context, ImplementationMember member);

	R visitInnerDefinition(C context, InnerDefinitionMember member);

	R visitStaticInitializer(C context, StaticInitializerMember member);
}
