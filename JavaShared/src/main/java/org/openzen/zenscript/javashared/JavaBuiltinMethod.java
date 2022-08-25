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
	public <T> T compileVirtual(JavaMethodCompiler<T> compiler, TypeID returnType, Expression target, CallArguments arguments) {
		return compiler.builtinVirtualMethod(method, target, arguments);
	}

	@Override
	public <T> T compileStatic(JavaMethodCompiler<T> compiler, TypeID returnType, CallArguments arguments) {
		return compiler.builtinStaticMethod(method, arguments);
	}
}
