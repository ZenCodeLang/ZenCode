/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.Module;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hoofdgebruiker
 */
public class SimpleJavaCompileSpace implements JavaCompileSpace {
	private final Map<Module, JavaCompiledModule> modules = new HashMap<>();

	@Override
	public void register(JavaCompiledModule module) {
		modules.put(module.module, module);
	}

	@Override
	public JavaCompiledModule getCompiled(Module module) {
		return modules.get(module);
	}
}
