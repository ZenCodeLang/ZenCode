/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.constructor.module.ModuleReference;
import org.openzen.zenscript.constructor.module.logging.*;

/**
 * @author Hoofdgebruiker
 */
public class ModuleLoader {
	private final Map<String, SemanticModule> moduleCache = new HashMap<>();
	private final Map<String, ModuleReference> modules = new HashMap<>();

	private final HashSet<String> compilingModulesSet = new HashSet<>();
	private final Stack<String> compilingModulesStack = new Stack<>();

	private final ModuleLogger exceptionLogger;

	public ModuleLoader(ModuleLogger exceptionLogger) {
		this.exceptionLogger = exceptionLogger;
	}

	public SemanticModule getModule(String name) {
		if (moduleCache.containsKey(name))
			return moduleCache.get(name);

		if (!modules.containsKey(name))
			throw new ConstructorException("Module not found: " + name);

		if (compilingModulesSet.contains(name)) {
			StringBuilder stack = new StringBuilder();
			stack.append("Circular module reference loading ").append(name).append(":");
			for (int i = compilingModulesStack.size() - 1; i >= 0; i--) {
				stack.append("\n\t").append(compilingModulesStack.get(i));
			}
			throw new ConstructorException(stack.toString());
		}

		compilingModulesSet.add(name);
		compilingModulesStack.add(name);

		SemanticModule module = modules.get(name).load(this, exceptionLogger);
		moduleCache.put(name, module);

		compilingModulesSet.remove(name);
		compilingModulesStack.pop();
		return module;
	}

	public void register(String name, ModuleReference module) {
		modules.put(name, module);
	}
}
