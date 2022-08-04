package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;

public interface JavaMethodCompiler<T> {
	T nativeMethod(JavaNativeMethod method, TypeID returnType, CallArguments arguments);

	T builtinMethod(BuiltinMethodSymbol method, CallArguments arguments);
}
