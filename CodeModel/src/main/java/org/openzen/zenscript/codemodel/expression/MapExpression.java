package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.TypeID;

public class MapExpression extends Expression {
	public final Expression[] keys;
	public final Expression[] values;

	public MapExpression(
			CodePosition position,
			Expression[] keys,
			Expression[] values,
			TypeID type) {
		super(position, type, binaryThrow(position, multiThrow(position, keys), multiThrow(position, values)));

		this.keys = keys;
		this.values = values;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitMap(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitMap(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression[] tKeys = Expression.transform(keys, transformer);
		Expression[] tValues = Expression.transform(values, transformer);
		return tKeys == keys && tValues == values ? this : new MapExpression(position, tKeys, tValues, type);
	}
}
