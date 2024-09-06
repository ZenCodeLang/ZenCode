package org.openzen.zenscript.validator.visitors;

import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.expression.modifiable.*;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.analysis.ExpressionScope;

public class ModifiableExpressionValidator implements ModifiableExpressionVisitor<Void> {
	private final Validator validator;
	private final ExpressionScope scope;
	private final ExpressionValidator expressionValidator;

	public ModifiableExpressionValidator(Validator validator, ExpressionScope scope, ExpressionValidator expressionValidator) {
		this.validator = validator;
		this.scope = scope;
		this.expressionValidator = expressionValidator;
	}

	@Override
	public Void visitLocalVariable(ModifiableLocalVariableExpression expression) {
		if (expression.variable.isFinal) {
			validator.logError(expression.position, CompileErrors.cannotSetFinalVariable(expression.variable.name));
		}
		return null;
	}

	@Override
	public Void visitField(ModifiableFieldExpression expression) {
		if (expression.field.getModifiers().isFinal()) {
			validator.logError(expression.position, CompileErrors.cannotSetFinalField(expression.field.getName()));
		}
		expression.target.accept(expressionValidator);
		return null;
	}

	@Override
	public Void visitFunctionParameter(ModifiableFunctionParameterExpression expression) {
		return null;
	}

	@Override
	public Void visitInvalid(ModifiableInvalidExpression expression) {
		validator.logError(expression.position, expression.error);
		return null;
	}

	@Override
	public Void visitProperty(ModifiablePropertyExpression expression) {
		TypeID getterType = expression.getter.getHeader().getReturnType();
		TypeID setterType = expression.setter.getHeader().getParameter(false, 0).type;
		if (!getterType.equals(setterType)) {
			validator.logError(expression.instance.position, CompileErrors.invalidPropertyPair(expression.instance.type, expression.getter.method.getID().toString()));
		}
		expression.instance.accept(expressionValidator);
		return null;
	}

	@Override
	public Void visitStaticField(ModifiableStaticFieldExpression expression) {
		return null;
	}
}
