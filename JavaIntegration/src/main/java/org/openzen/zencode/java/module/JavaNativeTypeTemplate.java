package org.openzen.zencode.java.module;

import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaNativeTypeTemplate {
	private final JavaNativeModule module;
	private final Class<?> cls;

	private List<MethodSymbol> constructors;

	public JavaNativeTypeTemplate(JavaNativeModule module, Class<?> cls) {
		this.module = module;
		this.cls = cls;
	}

	public List<MethodSymbol> getConstructors() {
		if (constructors == null) {
			List<MethodSymbol> result = new ArrayList<>();
			for (Constructor<?> constructor : cls.getConstructors()) {
				module.
			}
		}
	}
}
