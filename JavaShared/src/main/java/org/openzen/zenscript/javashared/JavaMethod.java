package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.type.TypeID;

public interface JavaMethod {
	<T> T compile(JavaMethodCompiler<T> compiler, TypeID returnType, CallArguments arguments);
}
