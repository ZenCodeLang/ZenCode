package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 * Using to cast a class type to a base type.
 */
public class SupertypeCastExpression extends Expression {
	public final Expression value;

	public SupertypeCastExpression(CodePosition position, Expression value, TypeID type) {
		super(position, type, value.thrownType);

		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSupertypeCast(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitSupertypeCast(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new SupertypeCastExpression(position, tValue, type);
	}
}
