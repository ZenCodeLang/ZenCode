package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.type.builtin.*;

public class JavaBuiltinModule {
	public static JavaCompiledModule generate() {
		JavaCompiledModule result = new JavaCompiledModule(Module.BUILTIN, FunctionParameter.NONE);
		for (BuiltinMethodSymbol builtin : BuiltinMethodSymbol.values()) {
			result.setMethodInfo(builtin, new JavaBuiltinMethod(builtin));
		}

		result.setMethodInfo(BuiltinMethodSymbol.BOOL_TO_STRING, JavaNativeMethod.getNativeStatic(JavaClass.BOOLEAN, "toString", "(Z)Ljava/lang/String;"));
		result.setMethodInfo(BuiltinMethodSymbol.BOOL_PARSE, JavaNativeMethod.getNativeStatic(JavaClass.BOOLEAN, "parseBoolean", "(Ljava/lang/String;)Z"));

		JavaNativeMethod divideUnsigned = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "divideUnsigned", "(II)I");
		result.setMethodInfo(BuiltinMethodSymbol.BYTE_DIV_BYTE, divideUnsigned);
		result.setMethodInfo(BuiltinMethodSymbol.USHORT_DIV_USHORT, divideUnsigned);
		result.setMethodInfo(BuiltinMethodSymbol.UINT_DIV_UINT, divideUnsigned);

		JavaNativeMethod remainderUnsigned = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "remainderUnsigned", "(II)I");
		result.setMethodInfo(BuiltinMethodSymbol.BYTE_MOD_BYTE, remainderUnsigned);
		result.setMethodInfo(BuiltinMethodSymbol.USHORT_MOD_USHORT, remainderUnsigned);
		result.setMethodInfo(BuiltinMethodSymbol.UINT_MOD_UINT, remainderUnsigned);

		return result;
	}

	private JavaBuiltinModule() {}
}
