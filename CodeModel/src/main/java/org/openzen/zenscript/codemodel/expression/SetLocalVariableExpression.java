package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.VarStatement;

public class SetLocalVariableExpression extends Expression {
	public final VarStatement variable;
	public final Expression value;
	
	public SetLocalVariableExpression(CodePosition position, VarStatement variable, Expression value) {
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

	@Override
	public Expression normalize(TypeScope scope) {
		return new SetLocalVariableExpression(position, variable, value.normalize(scope));
	}
}
