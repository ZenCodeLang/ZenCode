package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;

public class CallStaticExpression extends Expression {
	public final MethodInstance member;
	public final TypeID target;
	public final CallArguments arguments;

	public CallStaticExpression(CodePosition position, MethodInstance method, CallArguments arguments) {
		super(position, method.getHeader().getReturnType(), multiThrow(position, arguments.arguments));

		this.member = method;
		this.target = method.getTarget();
		this.arguments = arguments;
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
		return arguments == tArguments ? this : new CallStaticExpression(position, member, tArguments);
	}
}
