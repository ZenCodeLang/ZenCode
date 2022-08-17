package org.openzen.zenscript.codemodel.compilation.impl;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

public class BoundInstanceCallable implements CompilingCallable {
	private final ExpressionCompiler compiler;
	private final InstanceCallable base;
	private final Expression target;
	private final TypeID[] typeArguments;

	public BoundInstanceCallable(ExpressionCompiler compiler, InstanceCallable base, Expression target, TypeID[] typeArguments) {
		this.compiler = compiler;
		this.base = base;
		this.target = target;
		this.typeArguments = typeArguments;
	}

	@Override
	public Expression call(CodePosition position, CompilingExpression[] arguments) {
		return base.call(compiler, position, target, typeArguments, arguments);
	}

	@Override
	public CastedExpression casted(CodePosition position, CastedEval cast, CompilingExpression[] arguments) {
		return base.cast(compiler, position, cast, target, typeArguments, arguments);
	}
}
