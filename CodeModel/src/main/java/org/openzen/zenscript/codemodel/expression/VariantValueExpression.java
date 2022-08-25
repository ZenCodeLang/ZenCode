package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

public class VariantValueExpression extends Expression {
	public final VariantOptionInstance option;
	public final Expression[] arguments;

	public VariantValueExpression(CodePosition position, TypeID variantType, VariantOptionInstance option) {
		this(position, variantType, option, Expression.NONE);
	}

	public VariantValueExpression(CodePosition position, TypeID variantType, VariantOptionInstance option, Expression[] arguments) {
		super(position, variantType, multiThrow(position, arguments));

		this.option = option;
		this.arguments = arguments;
	}

	public int getNumberOfArguments() {
		return arguments == null ? 0 : arguments.length;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitVariantValue(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitVariantValue(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression[] tArguments = Expression.transform(arguments, transformer);
		return tArguments == arguments ? this : new VariantValueExpression(position, type, option, tArguments);
	}
}
