package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.javashared.JavaClass;

import java.util.HashMap;
import java.util.Map;

public class JavaCompilingClass {
	public final JavaClass compiled;
	private final Map<MethodSymbol, JavaCompilingMethod> methods = new HashMap<>();

	public JavaCompilingClass(JavaClass compiled) {
		this.compiled = compiled;
	}

	public void addMethod(MethodSymbol method, JavaCompilingMethod compiling) {
		methods.put(method, compiling);
	}

	public JavaCompilingMethod getMethod(MethodSymbol method) {
		return methods.get(method);
	}
}
