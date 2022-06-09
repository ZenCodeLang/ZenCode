package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;

public class GetterExpression extends Expression {
	public final Expression target;
	public final MethodInstance getter;

	public GetterExpression(CodePosition position, Expression target, MethodInstance getter) {
		super(position, getter.getHeader().getReturnType(), target.thrownType);

		this.target = target;
		this.getter = getter;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGetter(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitGetter(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tTarget = target.transform(transformer);
		return target == tTarget ? this : new GetterExpression(position, tTarget, getter);
	}
}
