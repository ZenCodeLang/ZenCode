package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;

public class SetterExpression extends Expression {
	public final Expression target;
	public final MethodInstance setter;
	public final Expression value;

	public SetterExpression(CodePosition position, Expression target, MethodInstance setter, Expression value) {
		super(position, setter.getHeader().parameters[0].type, value.thrownType);

		this.target = target;
		this.setter = setter;
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSetter(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitSetter(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tTarget = target.transform(transformer);
		Expression tValue = value.transform(transformer);
		return tTarget == target && tValue == value ? this : new SetterExpression(position, tTarget, setter, tValue);
	}
}
