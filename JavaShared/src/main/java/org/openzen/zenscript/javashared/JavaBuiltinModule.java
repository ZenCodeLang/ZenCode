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

		JavaNativeMethod intHighestOneBit = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "highestOneBit", "(I)I");
		result.setMethodInfo(BuiltinMethodSymbol.INT_HIGHEST_ONE_BIT, intHighestOneBit);
		result.setMethodInfo(BuiltinMethodSymbol.UINT_HIGHEST_ONE_BIT, intHighestOneBit);
		result.setMethodInfo(BuiltinMethodSymbol.USIZE_HIGHEST_ONE_BIT, intHighestOneBit);

		JavaNativeMethod intLowestOneBit = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "lowestOneBit", "(I)I");
		result.setMethodInfo(BuiltinMethodSymbol.INT_LOWEST_ONE_BIT, intLowestOneBit);
		result.setMethodInfo(BuiltinMethodSymbol.UINT_LOWEST_ONE_BIT, intLowestOneBit);
		result.setMethodInfo(BuiltinMethodSymbol.USIZE_LOWEST_ONE_BIT, intLowestOneBit);

		JavaNativeMethod intBitCount = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "bitCount", "(I)I");
		result.setMethodInfo(BuiltinMethodSymbol.INT_BIT_COUNT, intBitCount);
		result.setMethodInfo(BuiltinMethodSymbol.UINT_BIT_COUNT, intBitCount);
		result.setMethodInfo(BuiltinMethodSymbol.USIZE_BIT_COUNT, intBitCount);

		JavaNativeMethod longHighestOneBit = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "highestOneBit", "(J)J");
		result.setMethodInfo(BuiltinMethodSymbol.LONG_HIGHEST_ONE_BIT, longHighestOneBit);
		result.setMethodInfo(BuiltinMethodSymbol.ULONG_HIGHEST_ONE_BIT, longHighestOneBit);

		JavaNativeMethod longLowestOneBit = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "lowestOneBit", "(J)J");
		result.setMethodInfo(BuiltinMethodSymbol.LONG_LOWEST_ONE_BIT, longLowestOneBit);
		result.setMethodInfo(BuiltinMethodSymbol.ULONG_LOWEST_ONE_BIT, longLowestOneBit);

		JavaNativeMethod longBitCount = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "bitCount", "(J)I");
		result.setMethodInfo(BuiltinMethodSymbol.LONG_BIT_COUNT, longBitCount);
		result.setMethodInfo(BuiltinMethodSymbol.ULONG_BIT_COUNT, longBitCount);

		JavaNativeMethod floatBits = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "floatToRawIntBits", "(F)I");
		result.setMethodInfo(BuiltinMethodSymbol.FLOAT_BITS, floatBits);

		JavaNativeMethod doubleBits = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "doubleToRawLongBits", "(D)J");
		result.setMethodInfo(BuiltinMethodSymbol.DOUBLE_BITS, doubleBits);

		JavaNativeMethod stringLength = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "length", "()I");
		result.setMethodInfo(BuiltinMethodSymbol.STRING_LENGTH, stringLength);

		JavaNativeMethod stringCharacters = JavaNativeMethod.getNativeVirtual(JavaClass.STRING, "toCharArray", "()[C");
		result.setMethodInfo(BuiltinMethodSymbol.STRING_CHARACTERS, stringCharacters);

		return result;
	}

	private JavaBuiltinModule() {}
}
