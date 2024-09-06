package org.openzen.zenscript.codemodel.expression.modifiable;

public interface ModifiableExpressionVisitor<T> {
	T visitLocalVariable(ModifiableLocalVariableExpression expression);

	T visitField(ModifiableFieldExpression expression);

	T visitFunctionParameter(ModifiableFunctionParameterExpression expression);

	T visitInvalid(ModifiableInvalidExpression modifiableInvalidExpression);

	T visitProperty(ModifiablePropertyExpression expression);

	T visitStaticField(ModifiableStaticFieldExpression expression);
}
