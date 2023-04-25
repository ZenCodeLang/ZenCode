package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.VariableDefinition;

public class SetLocalVariableExpression extends Expression {
	public final VariableDefinition variable;
	public final Expression value;

	public SetLocalVariableExpression(CodePosition position, VariableDefinition variable, Expression value) {
		super(position, variable.type, value.thrownType);

		this.variable = variable;
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSetLocalVariable(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitSetLocalVariable(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new SetLocalVariableExpression(position, variable, tValue);
	}
}
