package org.openzen.zenscript.codemodel.identifiers;

public class ModuleSymbol {
	public static final ModuleSymbol BUILTIN = new ModuleSymbol("builtin");

	public final String name;

	public ModuleSymbol(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
