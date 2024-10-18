package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;

public interface JavaMethod {
	<T> T compileConstructor(JavaMethodCompiler<T> compiler, TypeID type, CallArguments arguments);
	<T> T compileBaseConstructor(JavaMethodCompiler<T> compiler, TypeID type, CallArguments arguments);
	<T> T compileVirtual(JavaMethodCompiler<T> compiler, TypeID returnType, Expression target, CallArguments arguments);
	<T> T compileVirtualWithTargetOnTopOfStack(JavaMethodCompiler<T> compiler, TypeID returnType, CallArguments arguments);
	<T> T compileStatic(JavaMethodCompiler<T> compiler, TypeID returnType, CallArguments arguments);
	<T> T compileSpecial(JavaMethodCompiler<T> compiler, TypeID returnType, Expression target, CallArguments arguments);

	String getMapping(JavaClass class_);

	JavaCompilingMethod asCompilingMethod(JavaClass compiled, String signature);
}
