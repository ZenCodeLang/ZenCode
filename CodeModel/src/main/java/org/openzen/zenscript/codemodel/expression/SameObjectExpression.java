package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

public class SameObjectExpression extends Expression {
	public final Expression left;
	public final Expression right;
	public final boolean inverted;

	public SameObjectExpression(CodePosition position, Expression left, Expression right, boolean inverted) {
		super(position, BasicTypeID.BOOL, binaryThrow(position, left.thrownType, right.thrownType));

		this.left = left;
		this.right = right;
		this.inverted = inverted;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSameObject(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitSameObject(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tLeft = left.transform(transformer);
		Expression tRight = right.transform(transformer);
		return tLeft == left && tRight == right ? this : new SameObjectExpression(position, tLeft, tRight, inverted);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new SameObjectExpression(position, left.normalize(scope), right.normalize(scope), inverted);
	}
}
