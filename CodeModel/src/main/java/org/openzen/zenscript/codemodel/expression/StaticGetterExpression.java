package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;

public class StaticGetterExpression extends Expression {
	public final MethodInstance getter;

	public StaticGetterExpression(CodePosition position, MethodInstance getter) {
		super(position, getter.getHeader().getReturnType(), null);

		this.getter = getter;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitStaticGetter(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitStaticGetter(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}
}
