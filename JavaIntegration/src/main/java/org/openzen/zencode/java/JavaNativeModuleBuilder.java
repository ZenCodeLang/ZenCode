package org.openzen.zencode.java;

import org.openzen.zencode.java.module.JavaNativeModule;
import org.openzen.zencode.java.module.JavaRuntimeClass;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.globals.IGlobal;

public interface JavaNativeModuleBuilder {
    JavaNativeModuleBuilder registerAdditionalClass(String packageName, Class<?> cls, JavaRuntimeClass runtimeClass);

    JavaNativeModuleBuilder addGlobals(Class<?> cls);

	JavaNativeModuleBuilder addGlobal(String name, IGlobal global);

	JavaNativeModuleBuilder addClass(Class<?> cls);

	JavaNativeModuleBuilder addDependency(JavaNativeModule dependency);

	JavaNativeModule getModuleUnderConstruction();

	JavaNativeModule complete() throws CompileException;
}
