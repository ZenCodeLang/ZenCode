package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;

public class GetStaticFieldExpression extends Expression {
	public final FieldInstance field;

	public GetStaticFieldExpression(CodePosition position, FieldInstance field) {
		super(position, field.getType(), null);

		this.field = field;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGetStaticField(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitGetStaticField(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}
}
