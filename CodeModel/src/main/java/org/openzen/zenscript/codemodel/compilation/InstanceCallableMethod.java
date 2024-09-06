package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ModificationExpression;
import org.openzen.zenscript.codemodel.expression.modifiable.ModifiableExpression;

public interface InstanceCallableMethod extends AnyMethod {
	Modifiers getModifiers();

	Expression call(ExpressionBuilder builder, Expression instance, CallArguments arguments);

	default Expression callModification(ExpressionBuilder builder, ModifiableExpression instance, ModificationExpression.Modification modification) {
		return builder.invalid(CompileErrors.invalidPostfix());
	}
}
