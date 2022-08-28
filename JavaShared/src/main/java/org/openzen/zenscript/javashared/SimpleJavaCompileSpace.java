/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hoofdgebruiker
 */
public class SimpleJavaCompileSpace implements JavaCompileSpace {
	private final Map<ModuleSymbol, JavaCompiledModule> modules = new HashMap<>();

	@Override
	public void register(JavaCompiledModule module) {
		modules.put(module.module, module);
	}

	@Override
	public JavaCompiledModule getCompiled(ModuleSymbol module) {
		return modules.get(module);
	}
}
