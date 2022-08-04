package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

public class AndAndExpression extends Expression {
	public final Expression left;
	public final Expression right;

	public AndAndExpression(CodePosition position, Expression left, Expression right) {
		super(position, BasicTypeID.BOOL, binaryThrow(position, left.thrownType, right.thrownType));

		this.left = left;
		this.right = right;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitAndAnd(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitAndAnd(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tLeft = left.transform(transformer);
		Expression tRight = right.transform(transformer);
		return tLeft == left && tRight == right
				? this
				: new AndAndExpression(position, tLeft, tRight);
	}
}
