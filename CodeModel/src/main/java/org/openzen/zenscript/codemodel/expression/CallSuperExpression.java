package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;

import java.util.Optional;

public class CallSuperExpression extends Expression {
	public final Expression target;
	public final MethodInstance method;
	public final CallArguments arguments;

	public CallSuperExpression(CodePosition position, Expression target, MethodInstance method, CallArguments arguments) {
		super(position, method.getHeader().getReturnType(), multiThrow(position, arguments.arguments));

		this.target = target;
		this.method = method;
		this.arguments = arguments;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCallSuper(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitCallSuper(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tTarget = target.transform(transformer);
		CallArguments tArguments = arguments.transform(transformer);
		return tTarget == target && tArguments == arguments
				? this
				: new CallSuperExpression(position, tTarget, method, tArguments);
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
