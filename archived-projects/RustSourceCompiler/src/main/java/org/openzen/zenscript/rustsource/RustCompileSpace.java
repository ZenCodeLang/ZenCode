package org.openzen.zenscript.rustsource;

import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;

public interface RustCompileSpace {
	void register(RustSourceModule module);

	RustSourceModule getCompiled(ModuleSymbol module);
}
