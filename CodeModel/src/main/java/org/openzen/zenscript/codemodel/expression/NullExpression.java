package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

public class NullExpression extends Expression {
	public NullExpression(CodePosition position) {
		super(position, BasicTypeID.NULL, null);
	}

	public NullExpression(CodePosition position, TypeID optionalType) {
		super(position, optionalType, null);
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitNull(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitNull(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}
}
