package org.openzen.zenscript.rustsource;

import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;

public class RustSourceContext {
	public final ZSPackage modulePackage;
	public final String basePackage;
	public final IZSLogger logger;
	private final GlobalTypeRegistry registry;
	private final RustCompileSpace space;

	public RustSourceContext(RustCompileSpace space, ZSPackage modulePackage, String basePackage, IZSLogger logger) {
		this.logger = logger;
		this.space = space;
		this.registry = space.getRegistry();

		this.modulePackage = modulePackage;
		this.basePackage = basePackage;
	}

	public void addModule(Module module, RustSourceModule compiled) {
		space.register(compiled);
	}

	public RustSourceModule getRustModule(Module module) {
		return space.getCompiled(module);
	}
}
