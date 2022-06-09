package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.member.ref.FieldMemberRef;

public class SetFieldExpression extends Expression {
	public final Expression target;
	public final FieldMemberRef field;
	public final Expression value;
	public final FunctionParameter parameter;

	public SetFieldExpression(CodePosition position, Expression target, FieldMemberRef field, Expression value) {
		super(position, field.getType(), binaryThrow(position, target.thrownType, value.thrownType));

		this.target = target;
		this.field = field;
		this.value = value;
		this.parameter = new FunctionParameter(type);
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSetField(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitSetField(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tTarget = target.transform(transformer);
		Expression tValue = value.transform(transformer);
		return tTarget == target && tValue == value
				? this
				: new SetFieldExpression(position, tTarget, field, tValue);
	}
}
