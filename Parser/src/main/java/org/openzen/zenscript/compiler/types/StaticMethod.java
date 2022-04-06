package org.openzen.zenscript.compiler.types;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;

public interface StaticMethod {
	FunctionHeader getHeader();

	Expression call(CallArguments arguments);
}
