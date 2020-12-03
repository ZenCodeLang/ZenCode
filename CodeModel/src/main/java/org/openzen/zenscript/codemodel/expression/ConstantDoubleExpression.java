package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

public class ConstantDoubleExpression extends Expression {
	public final double value;
	
	public ConstantDoubleExpression(CodePosition position, double value) {
		super(position, BasicTypeID.DOUBLE, null);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConstantDouble(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitConstantDouble(context, this);
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
