package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.TypeID;

public class RangeExpression extends Expression {
	public final Expression from;
	public final Expression to;

	public RangeExpression(CodePosition position, TypeID type, Expression from, Expression to) {
		super(position, type, binaryThrow(position, from.thrownType, to.thrownType));

		this.from = from;
		this.to = to;
	}

	private RangeExpression(CodePosition position, TypeID type, Expression from, Expression to, TypeID thrownType) {
		super(position, type, thrownType);

		this.from = from;
		this.to = to;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitRange(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitRange(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tFrom = from.transform(transformer);
		Expression tTo = to.transform(transformer);
		return tFrom == from && tTo == to ? this : new RangeExpression(position, type, tFrom, tTo, thrownType);
	}
}
