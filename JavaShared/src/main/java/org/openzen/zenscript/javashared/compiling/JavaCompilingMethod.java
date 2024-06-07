package org.openzen.zenscript.javashared.compiling;

import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaNativeMethod;

public class JavaCompilingMethod {
	public final JavaClass class_;
	public final JavaNativeMethod compiled;
	public boolean compile;
	public final String signature;

	public JavaCompilingMethod(JavaClass class_, JavaNativeMethod compiled, String signature) {
		this.class_ = class_;
		this.compiled = compiled;
		this.signature = signature;
		this.compile = true;
	}

	public JavaCompilingMethod(JavaClass class_, String signature) {
		this.class_ = class_;
		this.compiled = null;
		this.signature = signature;
		this.compile = false;
	}
}
