package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;

public class WrapOptionalExpression extends Expression {
	public final Expression value;

	public WrapOptionalExpression(CodePosition position, Expression value, TypeID optionalType) {
		super(position, optionalType, value.thrownType);

		if (value.type.isOptional())
			throw new IllegalArgumentException("Value is already optional");

		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitWrapOptional(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitWrapOptional(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new WrapOptionalExpression(position, tValue, type);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new WrapOptionalExpression(position, value.normalize(scope), type);
	}
}
