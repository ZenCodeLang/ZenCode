package org.openzen.zenscript.codemodel.compilation.impl;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.math.MathContext;

public class BoundSuperCallable implements CompilingCallable {
	private final ExpressionCompiler compiler;
	private final InstanceCallable method;
	private final Expression target;
	private final TypeID[] typeArguments;

	public BoundSuperCallable(ExpressionCompiler compiler, InstanceCallable method, Expression target, TypeID[] typeArguments) {
		this.compiler = compiler;
		this.method = method;
		this.target = target;
		this.typeArguments = typeArguments;
	}

	@Override
	public Expression call(CodePosition position, CompilingExpression[] arguments) {
		MatchedCallArguments<InstanceCallableMethod> matched = MatchedCallArguments.match(compiler, position, method.overloads, null, typeArguments, arguments);
		return matched.eval(compiler.at(position), (builder, method, args) -> builder.callSuper((MethodInstance) method, target, args));
	}

	@Override
	public CastedExpression casted(CodePosition position, CastedEval cast, CompilingExpression[] arguments) {
		MatchedCallArguments<InstanceCallableMethod> matched = MatchedCallArguments.match(compiler, position, method.overloads, cast.type, typeArguments, arguments);
		return matched.cast(compiler.at(position), cast, (builder, method, args) -> builder.callSuper((MethodInstance) method, target, args));
	}
}
