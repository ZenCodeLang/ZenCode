package org.openzen.zencode.java;

import org.openzen.zencode.java.module.JavaNativeModule;
import org.openzen.zencode.java.module.JavaRuntimeClass;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.globals.IGlobal;

import java.util.function.Function;

public interface JavaNativeModuleBuilder {
    JavaNativeModuleBuilder registerAdditionalClass(String packageName, Class<?> cls, Function<JavaNativeModule, JavaRuntimeClass> runtimeClassFactory);

    JavaNativeModuleBuilder addGlobals(Class<?> cls);

	JavaNativeModuleBuilder addGlobal(String name, IGlobal global);

	JavaNativeModuleBuilder addClass(Class<?> cls);

	JavaNativeModuleBuilder addDependency(JavaNativeModule dependency);

	JavaNativeModule complete() throws CompileException;
}
