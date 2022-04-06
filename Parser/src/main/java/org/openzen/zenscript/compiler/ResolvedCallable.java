package org.openzen.zenscript.compiler;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.TypeMatch;

public interface ResolvedCallable {
	Expression call(TypeID returnType, CompilingExpression... arguments);

	TypeMatch matches(TypeID returnType, CompilingExpression... arguments);

	InferredType inferReturnType(CompilingExpression... arguments);
}
