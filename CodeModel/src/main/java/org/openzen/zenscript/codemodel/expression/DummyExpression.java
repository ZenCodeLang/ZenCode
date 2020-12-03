package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;

public class DummyExpression extends Expression {
	public DummyExpression(TypeID type) {
		super(CodePosition.BUILTIN, type, null);
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		throw new UnsupportedOperationException("This is a dummy expression");
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		throw new UnsupportedOperationException("This is a dummy expression");
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		throw new UnsupportedOperationException("This is a dummy expression");
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return this;
	}
}
