package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

public interface JavaMethod {
	<T> T compileConstructor(JavaMethodCompiler<T> compiler, TypeID type, CallArguments arguments);
	<T> T compileVirtual(JavaMethodCompiler<T> compiler, TypeID returnType, Expression target, CallArguments arguments);
	<T> T compileStatic(JavaMethodCompiler<T> compiler, TypeID returnType, CallArguments arguments);
	<T> T compileSpecial(JavaMethodCompiler<T> compiler, TypeID returnType, Expression target, CallArguments arguments);

	String getMapping(JavaClass class_);
}
