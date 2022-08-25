package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;

/**
 * Converts a value from X? to X. Throws a NullPointerException if the value is null.
 */
public class CheckNullExpression extends Expression {
	public final Expression value;

	public CheckNullExpression(CodePosition position, Expression value) {
		super(position, value.type.withoutOptional(), value.thrownType);

		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCheckNull(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitCheckNull(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = transformer.transform(value);
		return value == tValue ? this : new CheckNullExpression(position, tValue);
	}
}
