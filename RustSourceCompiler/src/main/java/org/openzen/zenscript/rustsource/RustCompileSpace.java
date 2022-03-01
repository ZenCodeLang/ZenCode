package org.openzen.zenscript.rustsource;

import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;

public interface RustCompileSpace {
	void register(RustSourceModule module);

	GlobalTypeRegistry getRegistry();

	RustSourceModule getCompiled(Module module);
}
