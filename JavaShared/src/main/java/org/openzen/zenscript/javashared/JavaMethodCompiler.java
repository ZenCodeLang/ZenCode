package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;

public interface JavaMethodCompiler<T> {
	T nativeConstructor(JavaNativeMethod method, TypeID type, CallArguments arguments);

	T nativeVirtualMethod(JavaNativeMethod method, TypeID returnType, Expression target, CallArguments arguments);

	T nativeStaticMethod(JavaNativeMethod method, TypeID returnType, CallArguments arguments);

	T nativeSpecialMethod(JavaNativeMethod method, TypeID returnType, Expression target, CallArguments arguments);

	T builtinConstructor(BuiltinMethodSymbol method, TypeID type, CallArguments arguments);

	T builtinVirtualMethod(BuiltinMethodSymbol method, Expression target, CallArguments arguments);

	T builtinStaticMethod(BuiltinMethodSymbol method, TypeID returnType, CallArguments arguments);

	T specialConstructor(JavaSpecialMethod method, TypeID type, CallArguments arguments);

	T specialVirtualMethod(JavaSpecialMethod method, Expression target, CallArguments arguments);

	T specialStaticMethod(JavaSpecialMethod method, CallArguments arguments);
}
