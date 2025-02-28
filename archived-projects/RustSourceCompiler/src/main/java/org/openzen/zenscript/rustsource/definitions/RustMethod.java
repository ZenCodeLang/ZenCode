package org.openzen.zenscript.rustsource.definitions;

public class RustMethod {
	public final String name;
	public final boolean isPublic;

	public RustMethod(String name, boolean isPublic) {
		this.name = name;
		this.isPublic = isPublic;
	}
}
