package org.openzen.zenscript.javashared.compiling;

import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaNativeMethod;

public class JavaCompilingMethod {
	public final JavaClass class_;
	public final JavaNativeMethod compiled;
	public boolean compile;

	public JavaCompilingMethod(JavaClass class_, JavaNativeMethod compiled) {
		this.class_ = class_;
		this.compiled = compiled;
		this.compile = true;
	}
}
