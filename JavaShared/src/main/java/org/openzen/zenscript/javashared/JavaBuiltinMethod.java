package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;

public class JavaBuiltinMethod implements JavaMethod {
	private final BuiltinMethodSymbol method;

	public JavaBuiltinMethod(BuiltinMethodSymbol method) {
		this.method = method;
	}

	@Override
	public <T> T compile(JavaMethodCompiler<T> compiler, TypeID returnType, CallArguments arguments) {
		return compiler.builtinMethod(method, arguments);
	}
}
