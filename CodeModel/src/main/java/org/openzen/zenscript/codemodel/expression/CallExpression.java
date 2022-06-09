package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;

import java.util.Optional;

public class CallExpression extends Expression {
	public final Expression target;
	public final MethodInstance method;
	public final CallArguments arguments;
	public final FunctionHeader instancedHeader;

	public CallExpression(CodePosition position, Expression target, MethodInstance method, FunctionHeader instancedHeader, CallArguments arguments) {
		super(position, instancedHeader.getReturnType(), multiThrow(position, arguments.arguments));

		this.target = target;
		this.method = method;
		this.arguments = arguments;
		this.instancedHeader = instancedHeader;
	}

	public Expression getFirstArgument() {
		return arguments.arguments[0];
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCall(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitCall(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tTarget = target.transform(transformer);
		CallArguments tArguments = arguments.transform(transformer);
		return tTarget == target && tArguments == arguments
				? this
				: new CallExpression(position, tTarget, method, instancedHeader, tArguments);
	}

	@Override
	public Optional<CompileTimeConstant> evaluate() {
		CompileTimeConstant[] arguments = new CompileTimeConstant[this.arguments.arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			Optional<CompileTimeConstant> argument = this.arguments.arguments[i].evaluate();
			if (argument.isPresent()) {
				arguments[i] = argument.get();
			} else {
				return Optional.empty();
			}
		}

		return method.method.evaluate(this.arguments.typeArguments, arguments);
	}
}
