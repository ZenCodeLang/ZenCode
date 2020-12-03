package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

public class ConstantStringExpression extends Expression {
	public final String value;
	
	public ConstantStringExpression(CodePosition position, String value) {
		super(position, BasicTypeID.STRING, null);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConstantString(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitConstantString(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}
	
	@Override
	public String evaluateStringConstant() {
		return value;
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return this;
	}
}
