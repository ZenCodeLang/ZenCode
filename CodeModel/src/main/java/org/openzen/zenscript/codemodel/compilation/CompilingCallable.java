package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;

public interface CompilingCallable {
	Expression call(CodePosition position, CompilingExpression[] arguments);

	CastedExpression casted(CodePosition position, CastedEval cast, CompilingExpression[] arguments);
}
