package org.openzen.zenscript.compiler;

import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;

public class CompilerScope {
	public final GlobalTypeRegistry registry;
	public final Package rootPackage;

	public CompilerScope(GlobalTypeRegistry registry, Package rootPackage) {
		this.registry = registry;
		this.rootPackage = rootPackage;
	}
}
