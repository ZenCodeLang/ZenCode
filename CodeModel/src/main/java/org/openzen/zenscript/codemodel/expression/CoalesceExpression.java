package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;

public class CoalesceExpression extends Expression {
	public final Expression left;
	public final Expression right;
	
	public CoalesceExpression(CodePosition position, Expression left, Expression right) {
		super(position, right.type, binaryThrow(position, left.thrownType, right.thrownType));
		
		this.left = left;
		this.right = right;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCoalesce(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitCoalesce(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tLeft = left.transform(transformer);
		Expression tRight = right.transform(transformer);
		return tLeft == left && tRight == right ? this : new CoalesceExpression(position, tLeft, tRight);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new CoalesceExpression(position, left.normalize(scope), right.normalize(scope));
	}
}
