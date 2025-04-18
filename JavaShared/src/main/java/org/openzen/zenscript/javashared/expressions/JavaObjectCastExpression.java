package org.openzen.zenscript.javashared.expressions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.expression.ExpressionVisitor;
import org.openzen.zenscript.codemodel.expression.ExpressionVisitorWithContext;
import org.openzen.zenscript.codemodel.type.TypeID;

public class JavaObjectCastExpression extends Expression {
	public final Expression value;

	public JavaObjectCastExpression(CodePosition position, TypeID type, Expression value) {
		super(position, type, value.thrownType);

		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitPlatformSpecific(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitPlatformSpecific(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return new JavaObjectCastExpression(position, type, transformer.transform(value));
	}
}
