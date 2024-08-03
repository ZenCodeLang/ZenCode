package org.openzen.zencode.java.impl;

import org.openzen.zencode.java.module.JavaNativeModule;
import org.openzen.zenscript.codemodel.ModuleSpace;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JavaNativeModuleSpace {
	public final ModuleSpace moduleSpace;
	private final Map<String, JavaNativeModule> modulesByBasePackage = new HashMap<>();
	private final Map<String, JavaNativeModule> moduleByPackage = new HashMap<>();
	private final Map<Class<?>, JavaNativeModule> moduleByClass = new HashMap<>();

	public JavaNativeModuleSpace(ModuleSpace moduleSpace) {
		this.moduleSpace = moduleSpace;
	}

	public Optional<JavaNativeModule> getModule(Class<?> cls) {
		if (moduleByClass.containsKey(cls))
			return Optional.of(moduleByClass.get(cls));

		String package_ = cls.getPackage().getName();
		return Optional.ofNullable(moduleByPackage.computeIfAbsent(package_, this::getModuleHierarchical));
	}

	public void register(String base, JavaNativeModule module) {
		JavaNativeModule existing = getModuleHierarchical(base);
		if (existing != null)
			throw new IllegalArgumentException("This is already a module at " + base + ": " + existing.getModule().name);

		modulesByBasePackage.put(base, module);
	}

	public void registerClass(Class<?> cls, JavaNativeModule module) {
		moduleByClass.put(cls, module);
	}

	private JavaNativeModule getModuleHierarchical(String packageName) {
		if (modulesByBasePackage.containsKey(packageName)) {
			return modulesByBasePackage.get(packageName);
		}

		int lastDot = packageName.lastIndexOf('.');
		if (lastDot < 0) {
			return null;
		}
		String parent = packageName.substring(0, lastDot);
		return getModuleHierarchical(parent);
	}
}
