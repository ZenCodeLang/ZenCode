package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.TypeID;

public class GenericWildcardCastExpression extends Expression {
	public final Expression value;

	public GenericWildcardCastExpression(CodePosition position, Expression value, TypeID type) {
		super(position, type, value.thrownType);

		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGenericCast(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitGenericCast(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression transformedValue = transformer.transform(value);
		return transformedValue == value ? this : new GenericWildcardCastExpression(position, transformedValue, type);
	}
}
