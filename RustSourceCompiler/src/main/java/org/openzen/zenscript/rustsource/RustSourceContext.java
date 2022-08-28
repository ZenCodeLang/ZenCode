package org.openzen.zenscript.rustsource;

import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.definition.ZSPackage;

public class RustSourceContext {
	public final ZSPackage modulePackage;
	public final String basePackage;
	public final IZSLogger logger;
	private final RustCompileSpace space;

	public RustSourceContext(RustCompileSpace space, ZSPackage modulePackage, String basePackage, IZSLogger logger) {
		this.logger = logger;
		this.space = space;

		this.modulePackage = modulePackage;
		this.basePackage = basePackage;
	}

	public void addModule(ModuleSymbol module, RustSourceModule compiled) {
		space.register(compiled);
	}

	public RustSourceModule getRustModule(ModuleSymbol module) {
		return space.getCompiled(module);
	}
}
