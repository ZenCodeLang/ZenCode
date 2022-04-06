package org.openzen.zenscript.compiler.types;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.compiler.expression.CompilingExpression;

public interface ResolvedConstructor {
	Expression call(CompilingExpression... arguments);

	Expression superCall(CompilingExpression... arguments);
}
