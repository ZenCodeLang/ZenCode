package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.member.ref.FieldMemberRef;

public class SetStaticFieldExpression extends Expression {
	public final FieldMemberRef field;
	public final Expression value;

	public SetStaticFieldExpression(CodePosition position, FieldMemberRef field, Expression value) {
		super(position, field.getType(), value.thrownType);

		this.field = field;
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSetStaticField(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitSetStaticField(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		return value == tValue ? this : new SetStaticFieldExpression(position, field, tValue);
	}
}
