package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.TypeID;

public class PanicExpression extends Expression {
	public final Expression value;

	public PanicExpression(CodePosition position, TypeID type, Expression value) {
		super(position, type, null);

		this.value = value;
	}

	@Override
	public boolean aborts() {
		return true;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitPanic(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitPanic(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return new PanicExpression(position, type, transformer.transform(value));
	}
}
