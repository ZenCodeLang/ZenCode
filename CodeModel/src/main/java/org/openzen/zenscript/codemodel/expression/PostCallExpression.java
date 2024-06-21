package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;

/**
 * Used for post-increment and post-decrement.
 */
public class PostCallExpression extends Expression {
	public final Expression target;
	public final MethodInstance member;

	public PostCallExpression(CodePosition position, Expression target, MethodInstance member) {
		super(position, member.getHeader().getReturnType(), binaryThrow(position, member.getHeader().thrownType, target.thrownType));

		OperatorType operator = member.method.getID().getOperator().orElse(null);
		if (operator != OperatorType.DECREMENT && operator != OperatorType.INCREMENT)
			throw new IllegalArgumentException("Operator must be increment or decrement");

		this.target = target;
		this.member = member;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitPostCall(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitPostCall(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tTarget = target.transform(transformer);
		return target == tTarget ? this : new PostCallExpression(position, tTarget, member);
	}
}
