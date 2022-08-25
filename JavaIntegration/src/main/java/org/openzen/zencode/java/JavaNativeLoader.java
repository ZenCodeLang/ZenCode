/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.java;

import org.openzen.zencode.java.module.JavaNativeModule;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.definition.ZSPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Stan Hebben
 */
public class JavaNativeLoader {
	private final Class<?>[] classes;
	private final Class<?>[] globals;
	private final Map<String, LoadingModule> modulesByName = new HashMap<>();
	private final IZSLogger logger;
	private boolean loaded = false;

	public JavaNativeLoader(Class<?>[] classes, Class<?>[] globals, IZSLogger logger) {
		this.classes = classes;
		this.globals = globals;
		this.logger = logger;
	}

	public LoadingModule addModule(
			ZSPackage pkg,
			String name,
			String basePackage,
			String... dependencies) {
		if (loaded)
			throw new IllegalStateException("Already loaded modules!");
		if (modulesByName.containsKey(name))
			throw new IllegalArgumentException("Module already exists: " + name);

		LoadingModule module = new LoadingModule(pkg, name, basePackage, dependencies);
		modulesByName.put(name, module);

		return module;
	}

	public ScriptingEngine load() throws CompileException {
		loaded = true;

		sortClasses();

		ScriptingEngine engine = new ScriptingEngine();
		for (LoadingModule module : modulesByName.values()) {
			load(engine, module);
		}
		return engine;
	}

	private void sortClasses() throws CompileException {
		Map<String, LoadingModule> modulesByPackage = new HashMap<>();
		for (LoadingModule module : modulesByName.values()) {
			if (modulesByPackage.containsKey(module.basePackage))
				throw CompileException.internalError("Two modules have the same base package: " + module.basePackage);

			modulesByPackage.put(module.basePackage, module);
		}

		for (Class<?> cls : classes) {
			LoadingModule module = findModuleByPackage(modulesByPackage, getPackageName(cls));
			if (module == null) {
				logger.warning("Module not found for class " + cls.getName());
			} else {
				module.classes.add(cls);
			}
		}
		for (Class<?> cls : globals) {
			LoadingModule module = findModuleByPackage(modulesByPackage, getPackageName(cls));
			if (module == null) {
				logger.warning("Module not found for class " + cls.getName());
			} else {
				module.globals.add(cls);
			}
		}
	}

	private String getPackageName(Class<?> cls) {
		String name = cls.getName();
		return name.substring(0, name.lastIndexOf('.'));
	}

	private LoadingModule findModuleByPackage(Map<String, LoadingModule> modulesByPackage, String packageName) {
		if (modulesByPackage.containsKey(packageName))
			return modulesByPackage.get(packageName);

		int index = packageName.lastIndexOf('.');
		if (index < 0)
			return null;

		return findModuleByPackage(modulesByPackage, packageName.substring(0, index));
	}

	private JavaNativeModule load(ScriptingEngine engine, LoadingModule module) throws CompileException {
		if (module.resolved != null)
			return module.resolved;

		JavaNativeModule[] dependencies = new JavaNativeModule[module.dependencies.length];
		for (int i = 0; i < dependencies.length; i++) {
			LoadingModule dependency = modulesByName.get(module.dependencies[i]);
			if (dependency == null)
				throw CompileException.internalError("Module dependency for " + module.name + " missing: " + module.dependencies[i]);

			dependencies[i] = load(engine, dependency);
		}

		module.resolved = new JavaNativeModule(
				logger,
				module.pkg,
				module.name,
				module.basePackage,
				dependencies,
				engine.root);
		for (Class<?> cls : module.classes)
			module.resolved.addClass(cls);
		for (Class<?> cls : module.globals)
			module.resolved.addGlobals(cls);
		for (Consumer<JavaNativeModule> resolved : module.whenResolved)
			resolved.accept(module.resolved);

		engine.registerNativeProvided(module.resolved);
		return module.resolved;
	}

	public static class LoadingModule {
		private final ZSPackage pkg;
		private final String name;
		private final String basePackage;
		private final String[] dependencies;
		private final List<Consumer<JavaNativeModule>> whenResolved = new ArrayList<>();
		private final List<Class<?>> classes = new ArrayList<>();
		private final List<Class<?>> globals = new ArrayList<>();
		private JavaNativeModule resolved;

		private LoadingModule(ZSPackage pkg, String name, String basePackage, String[] dependencies) {
			this.pkg = pkg;
			this.name = name;
			this.basePackage = basePackage;
			this.dependencies = dependencies;
		}

		public void whenLoaded(Consumer<JavaNativeModule> consumer) {
			whenResolved.add(consumer);
		}
	}
}
