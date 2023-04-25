package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.VariableDefinition;

public class GetLocalVariableExpression extends Expression {
	public final VariableDefinition variable;

	public GetLocalVariableExpression(CodePosition position, VariableDefinition variable) {
		super(position, variable.type, null);

		this.variable = variable;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGetLocalVariable(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitGetLocalVariable(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}
}
