package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinFieldSymbol;

public interface JavaFieldCompiler<T> {
	T nativeInstanceGet(JavaNativeField field, Expression instance);

	T nativeInstanceSet(JavaNativeField field, Expression instance, Expression value);

	T nativeStaticGet(JavaNativeField field);

	T nativeStaticSet(JavaNativeField field, Expression value);

	T builtinInstanceGet(BuiltinFieldSymbol field, Expression instance);

	T builtinInstanceSet(BuiltinFieldSymbol field, Expression instance, Expression value);

	T builtinStaticGet(BuiltinFieldSymbol field);

	T builtinStaticSet(BuiltinFieldSymbol field, Expression value);
}
