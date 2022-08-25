package org.openzen.zenscript.rustsource;

import org.openzen.zenscript.codemodel.Module;

public interface RustCompileSpace {
	void register(RustSourceModule module);

	RustSourceModule getCompiled(Module module);
}
