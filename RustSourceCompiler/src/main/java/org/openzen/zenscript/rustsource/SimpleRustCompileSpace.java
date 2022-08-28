package org.openzen.zenscript.rustsource;

import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;

import java.util.HashMap;
import java.util.Map;

public class SimpleRustCompileSpace implements RustCompileSpace {
	private final Map<ModuleSymbol, RustSourceModule> modules = new HashMap<>();

	@Override
	public void register(RustSourceModule module) {
		modules.put(module.module, module);
	}

	@Override
	public RustSourceModule getCompiled(ModuleSymbol module) {
		return modules.get(module);
	}
}
