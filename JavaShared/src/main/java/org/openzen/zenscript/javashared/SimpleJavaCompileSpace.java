/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hoofdgebruiker
 */
public class SimpleJavaCompileSpace implements JavaCompileSpace {
	private final GlobalTypeRegistry registry;
	private final Map<Module, JavaCompiledModule> modules = new HashMap<>();

	public SimpleJavaCompileSpace(GlobalTypeRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void register(JavaCompiledModule module) {
		modules.put(module.module, module);
	}

	@Override
	public GlobalTypeRegistry getRegistry() {
		return registry;
	}

	@Override
	public JavaCompiledModule getCompiled(Module module) {
		return modules.get(module);
	}
}
