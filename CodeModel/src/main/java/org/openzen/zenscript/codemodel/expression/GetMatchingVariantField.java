package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.scope.TypeScope;

public class GetMatchingVariantField extends Expression {
	public final VariantOptionSwitchValue value;
	public final int index;
	
	public GetMatchingVariantField(CodePosition position, VariantOptionSwitchValue value, int index) {
		super(position, value.option.types[index], null);
		
		this.value = value;
		this.index = index;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGetMatchingVariantField(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitGetMatchingVariantField(context, this);
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
