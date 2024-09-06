package org.openzen.zenscript.codemodel.expression.modifiable;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.VariableDefinition;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ModifiableLocalVariableExpression implements ModifiableExpression {
	public final CodePosition position;
	public final VariableDefinition variable;

	public ModifiableLocalVariableExpression(CodePosition position, VariableDefinition variable) {
		this.position = position;
		this.variable = variable;
	}

	@Override
	public TypeID getType() {
		return variable.type;
	}

	@Override
	public <T> T accept(ModifiableExpressionVisitor<T> visitor) {
		return visitor.visitLocalVariable(this);
	}

	@Override
	public <C, R> R accept(C context, ModifiableExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitLocalVariable(context, this);
	}

	@Override
	public ModifiableExpression transform(ExpressionTransformer transformer) {
		return this;
	}
}
