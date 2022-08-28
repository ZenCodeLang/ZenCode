package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.Expression;

public interface JavaField {
	<T> T compileInstanceGet(JavaFieldCompiler<T> compiler, Expression target);

	<T> T compileInstanceSet(JavaFieldCompiler<T> compiler, Expression target, Expression value);

	<T> T compileStaticGet(JavaFieldCompiler<T> compiler);

	<T> T compileStaticSet(JavaFieldCompiler<T> compiler, Expression value);

	String getMapping(JavaClass definition);
}
