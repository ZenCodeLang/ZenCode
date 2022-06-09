package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ConstructorThisCallExpression extends Expression {
	public final TypeID objectType;
	public final FunctionalMemberRef constructor;
	public final CallArguments arguments;

	public ConstructorThisCallExpression(CodePosition position, TypeID type, FunctionalMemberRef constructor, CallArguments arguments) {
		super(position, BasicTypeID.VOID, binaryThrow(position, constructor.getHeader().thrownType, multiThrow(position, arguments.arguments)));

		if (type instanceof BasicTypeID)
			throw new IllegalArgumentException("Type cannot be basic type");

		this.objectType = type;
		this.constructor = constructor;
		this.arguments = arguments;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConstructorThisCall(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitConstructorThisCall(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		CallArguments tArguments = arguments.transform(transformer);
		return tArguments == arguments ? this : new ConstructorThisCallExpression(position, objectType, constructor, tArguments);
	}
}
