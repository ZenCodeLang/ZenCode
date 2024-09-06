package org.openzen.zenscript.codemodel.expression.modifiable;

public interface ModifiableExpressionVisitorWithContext<C, R> {
	R visitLocalVariable(C context, ModifiableLocalVariableExpression expression);

	R visitField(C context, ModifiableFieldExpression expression);

	R visitFunctionParameter(C context, ModifiableFunctionParameterExpression expression);

	R visitInvalid(C context, ModifiableInvalidExpression modifiableInvalidExpression);

	R visitProperty(C context, ModifiablePropertyExpression expression);

	R visitStaticField(C context, ModifiableStaticFieldExpression expression);
}
