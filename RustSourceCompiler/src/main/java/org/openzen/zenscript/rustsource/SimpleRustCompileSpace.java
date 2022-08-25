package org.openzen.zenscript.rustsource;

import org.openzen.zenscript.codemodel.Module;

import java.util.HashMap;
import java.util.Map;

public class SimpleRustCompileSpace implements RustCompileSpace {
	private final Map<Module, RustSourceModule> modules = new HashMap<>();

	@Override
	public void register(RustSourceModule module) {
		modules.put(module.module, module);
	}

	@Override
	public RustSourceModule getCompiled(Module module) {
		return modules.get(module);
	}
}
