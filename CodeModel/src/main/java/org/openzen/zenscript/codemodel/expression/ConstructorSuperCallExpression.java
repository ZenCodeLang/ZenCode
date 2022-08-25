package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ConstructorSuperCallExpression extends Expression {
	public final TypeID objectType;
	public final MethodInstance constructor;
	public final CallArguments arguments;

	public ConstructorSuperCallExpression(CodePosition position, TypeID type, MethodInstance constructor, CallArguments arguments) {
		super(position, BasicTypeID.VOID, binaryThrow(position, constructor.getHeader().thrownType, multiThrow(position, arguments.arguments)));

		this.objectType = type;
		this.constructor = constructor;
		this.arguments = arguments;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConstructorSuperCall(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitConstructorSuperCall(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		CallArguments tArguments = arguments.transform(transformer);
		return tArguments == arguments ? this : new ConstructorSuperCallExpression(position, type, constructor, tArguments);
	}
}
