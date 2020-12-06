package org.openzen.zenscript.codemodel;

public class Module {
	public static final Module BUILTIN = new Module("builtin");

	public final String name;

	public Module(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
