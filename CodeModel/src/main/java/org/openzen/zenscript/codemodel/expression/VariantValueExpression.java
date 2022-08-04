package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionInstance;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;

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
	public Expression call(CodePosition position, TypeScope scope, List<TypeID> hints, CallArguments arguments) throws CompileException {
		if (arguments != null)
			return super.call(position, scope, hints, arguments);

		return new VariantValueExpression(position, type, option, arguments.arguments);
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
