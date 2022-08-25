package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

public class StaticCompilingCallable implements CompilingCallable {
	private final ExpressionCompiler compiler;
	private final StaticCallable target;

	public StaticCompilingCallable(ExpressionCompiler compiler, StaticCallable target) {
		this.compiler = compiler;
		this.target = target;
	}

	@Override
	public Expression call(CodePosition position, CompilingExpression[] arguments) {
		return target.call(compiler, position, TypeID.NONE, arguments);
	}

	@Override
	public CastedExpression casted(CodePosition position, CastedEval cast, CompilingExpression[] arguments) {
		return target.casted(compiler, position, cast, TypeID.NONE, arguments);
	}
}
