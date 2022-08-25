package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.javashared.JavaNativeMethod;

public class JavaCompilingMethod {
	public final JavaCompilingClass class_;
	public final JavaNativeMethod compiled;
	public boolean compile;

	public JavaCompilingMethod(JavaCompilingClass class_, JavaNativeMethod compiled) {
		this.class_ = class_;
		this.compiled = compiled;
		this.compile = true;
	}
}
