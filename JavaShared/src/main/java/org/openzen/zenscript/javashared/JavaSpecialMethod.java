package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.type.TypeID;

public class JavaSpecialMethod implements JavaMethod {
	public enum Method {
		STRINGBUILDER_ISEMPTY
	}

	@Override
	public <T> T compile(JavaMethodCompiler<T> compiler, TypeID returnType, CallArguments arguments) {
		return null;
	}
}
