package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ThrowExpression extends Expression {
	public final Expression value;

	public ThrowExpression(CodePosition position, TypeID type, Expression value) {
		super(position, type, value.type);

		this.value = value;
	}

	@Override
	public boolean aborts() {
		return true;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitThrow(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitThrow(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = transformer.transform(value);
		return tValue == value ? this : new ThrowExpression(position, type, value);
	}
}
