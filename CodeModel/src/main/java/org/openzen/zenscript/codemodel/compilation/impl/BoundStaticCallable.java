package org.openzen.zenscript.codemodel.compilation.impl;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

public class BoundStaticCallable implements CompilingCallable {
	private final StaticCallable base;
	private final TypeID[] typeArguments;

	public BoundStaticCallable(StaticCallable base, TypeID[] typeArguments) {
		this.base = base;
		this.typeArguments = typeArguments;
	}

	@Override
	public Expression call(ExpressionCompiler compiler, CodePosition position, CompilingExpression[] arguments) {
		return base.call(compiler, position, typeArguments, arguments);
	}

	@Override
	public CastedExpression casted(ExpressionCompiler compiler, CodePosition position, CastedEval cast, CompilingExpression[] arguments) {
		return base.casted(compiler, position, cast, typeArguments, arguments);
	}
}
