package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;

public class StaticSetterExpression extends Expression {
	public final MethodInstance setter;
	public final Expression value;

	public StaticSetterExpression(CodePosition position, MethodInstance setter, Expression value) {
		super(position, value.type, value.thrownType);

		this.setter = setter;
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitStaticSetter(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitStaticSetter(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new StaticSetterExpression(position, setter, tValue);
	}
}
