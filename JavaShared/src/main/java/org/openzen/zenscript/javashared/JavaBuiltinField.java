package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinFieldSymbol;

public class JavaBuiltinField implements JavaField {
	public final BuiltinFieldSymbol field;

	public JavaBuiltinField(BuiltinFieldSymbol field) {
		this.field = field;
	}

	@Override
	public <T> T compileInstanceGet(JavaFieldCompiler<T> compiler, Expression target) {
		return compiler.builtinInstanceGet(field, target);
	}

	@Override
	public <T> T compileInstanceSet(JavaFieldCompiler<T> compiler, Expression target, Expression value) {
		return compiler.builtinInstanceSet(field, target, value);
	}

	@Override
	public <T> T compileStaticGet(JavaFieldCompiler<T> compiler) {
		return compiler.builtinStaticGet(field);
	}

	@Override
	public <T> T compileStaticSet(JavaFieldCompiler<T> compiler, Expression value) {
		return compiler.builtinStaticSet(field, value);
	}

	@Override
	public String getMapping(JavaClass definition) {
		StringBuilder result = new StringBuilder();
		result.append(field.getName());
		result.append(":builtin");
		return result.toString();
	}
}
