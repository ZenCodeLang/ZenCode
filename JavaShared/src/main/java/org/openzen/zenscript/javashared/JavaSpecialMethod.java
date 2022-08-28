package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

public enum JavaSpecialMethod implements JavaMethod {
	STRINGBUILDER_ISEMPTY,
	LIST_TO_ARRAY,
	CONTAINS_AS_INDEXOF,
	SORTED,
	SORTED_WITH_COMPARATOR,
	ARRAY_COPY,
	ARRAY_COPY_RESIZE,
	ARRAY_COPY_TO,
	STRING_TO_ASCII,
	STRING_TO_UTF8,
	BYTES_ASCII_TO_STRING,
	BYTES_UTF8_TO_STRING;

	@Override
	public <T> T compileConstructor(JavaMethodCompiler<T> compiler, TypeID type, CallArguments arguments) {
		return compiler.specialConstructor(this, type, arguments);
	}

	@Override
	public <T> T compileVirtual(JavaMethodCompiler<T> compiler, TypeID returnType, Expression target, CallArguments arguments) {
		return compiler.specialVirtualMethod(this, target, arguments);
	}

	@Override
	public <T> T compileStatic(JavaMethodCompiler<T> compiler, TypeID returnType, CallArguments arguments) {
		return compiler.specialStaticMethod(this, arguments);
	}

	@Override
	public String getMapping(JavaClass class_) {
		return class_.internalName + "::special::" + name();
	}
}
