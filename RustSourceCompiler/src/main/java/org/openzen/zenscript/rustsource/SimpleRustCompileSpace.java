package org.openzen.zenscript.rustsource;

import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;

import java.util.HashMap;
import java.util.Map;

public class SimpleRustCompileSpace implements RustCompileSpace {
	private final GlobalTypeRegistry registry;
	private final Map<Module, RustSourceModule> modules = new HashMap<>();

	public SimpleRustCompileSpace(GlobalTypeRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void register(RustSourceModule module) {
		modules.put(module.module, module);
	}

	@Override
	public GlobalTypeRegistry getRegistry() {
		return registry;
	}

	@Override
	public RustSourceModule getCompiled(Module module) {
		return modules.get(module);
	}
}
