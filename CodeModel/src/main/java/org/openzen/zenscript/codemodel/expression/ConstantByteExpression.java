package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

public class ConstantByteExpression extends Expression {
	public final int value;
	
	public ConstantByteExpression(CodePosition position, int value) {
		super(position, BasicTypeID.BYTE, null);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConstantByte(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitConstantByte(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return this;
	}
}
