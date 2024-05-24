package org.openzen.zencode.java.impl.conversion;

import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zenscript.javashared.JavaClass;

public class ConversionUtils {
	private ConversionUtils() {}

	public static JavaClass.Kind getKindFromAnnotations(Class<?> cls) {
		if (cls.isAnnotationPresent(ZenCodeType.Expansion.class)) {
			return JavaClass.Kind.EXPANSION;
		} else if (cls.isInterface()) {
			return JavaClass.Kind.INTERFACE;
		} else if (cls.isEnum()) {
			return JavaClass.Kind.ENUM;
		} else {
			return JavaClass.Kind.CLASS;
		}
	}
}
