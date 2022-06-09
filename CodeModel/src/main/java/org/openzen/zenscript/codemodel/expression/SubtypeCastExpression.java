package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 * Using to cast a base type to a class type.
 */
public class SubtypeCastExpression extends Expression {
	public final Expression value;

	public SubtypeCastExpression(CodePosition position, Expression value, TypeID type) {
		super(position, type, value.thrownType);

		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSubtypeCast(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitSubtypeCast(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new SubtypeCastExpression(position, tValue, type);
	}
}
