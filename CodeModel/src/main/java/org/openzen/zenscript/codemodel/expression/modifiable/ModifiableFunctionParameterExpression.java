package org.openzen.zenscript.codemodel.expression.modifiable;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ModifiableFunctionParameterExpression implements ModifiableExpression {
	public final FunctionParameter parameter;

	public ModifiableFunctionParameterExpression(FunctionParameter parameter) {
		this.parameter = parameter;
	}

	@Override
	public TypeID getType() {
		return parameter.type;
	}

	@Override
	public <T> T accept(ModifiableExpressionVisitor<T> visitor) {
		return visitor.visitFunctionParameter(this);
	}

	@Override
	public <C, R> R accept(C context, ModifiableExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitFunctionParameter(context, this);
	}

	@Override
	public ModifiableExpression transform(ExpressionTransformer transformer) {
		return this;
	}
}
