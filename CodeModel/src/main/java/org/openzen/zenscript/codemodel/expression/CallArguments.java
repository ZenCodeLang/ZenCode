package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.compilation.CastedExpression;
import org.openzen.zenscript.codemodel.type.TypeID;

public class CallArguments {
	public static final CallArguments EMPTY = new CallArguments(Expression.NONE);

	public final CastedExpression.Level level;
	public final TypeID[] typeArguments;
	public final Expression[] arguments;

	public CallArguments(Expression... arguments) {
		this.level = CastedExpression.Level.EXACT;
		this.typeArguments = TypeID.NONE;
		this.arguments = arguments;
	}

	public CallArguments(TypeID[] typeArguments, Expression[] arguments) {
		if (typeArguments == null)
			typeArguments = TypeID.NONE;
		if (arguments == null)
			throw new IllegalArgumentException("Arguments cannot be null!");

		this.level = CastedExpression.Level.EXACT;
		this.typeArguments = typeArguments;
		this.arguments = arguments;
	}

	public CallArguments(CastedExpression.Level level, TypeID[] typeArguments, Expression[] arguments) {
		if (typeArguments == null)
			typeArguments = TypeID.NONE;
		if (arguments == null)
			throw new IllegalArgumentException("Arguments cannot be null!");

		this.level = level;
		this.typeArguments = typeArguments;
		this.arguments = arguments;
	}

	public CallArguments(TypeID... dummy) {
		this.level = CastedExpression.Level.EXACT;
		this.typeArguments = TypeID.NONE;
		this.arguments = new Expression[dummy.length];
		for (int i = 0; i < dummy.length; i++)
			arguments[i] = new DummyExpression(dummy[i]);
	}

	public CallArguments bind(Expression target) {
		Expression[] newArguments = new Expression[arguments.length + 1];
		newArguments[0] = target;
		System.arraycopy(arguments, 0, newArguments, 1, arguments.length);
		return new CallArguments(typeArguments, newArguments);
	}

	public int getNumberOfTypeArguments() {
		return typeArguments.length;
	}

	public CallArguments transform(ExpressionTransformer transformer) {
		Expression[] tArguments = Expression.transform(arguments, transformer);
		return tArguments == arguments ? this : new CallArguments(level, typeArguments, tArguments);
	}
}
