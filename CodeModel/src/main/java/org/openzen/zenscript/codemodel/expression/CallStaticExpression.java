package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

public class CallStaticExpression extends Expression {
	public final MethodInstance member;
	public final TypeID target;
	public final CallArguments arguments;
	public final FunctionHeader instancedHeader;

	public CallStaticExpression(CodePosition position, MethodInstance method, FunctionHeader instancedHeader, CallArguments arguments) {
		super(position, instancedHeader.getReturnType(), multiThrow(position, arguments.arguments));

		this.member = method;
		this.target = method.getTarget();
		this.arguments = arguments;
		this.instancedHeader = instancedHeader;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCallStatic(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitCallStatic(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		CallArguments tArguments = arguments.transform(transformer);
		return arguments == tArguments ? this : new CallStaticExpression(position, member, instancedHeader, tArguments);
	}
}
