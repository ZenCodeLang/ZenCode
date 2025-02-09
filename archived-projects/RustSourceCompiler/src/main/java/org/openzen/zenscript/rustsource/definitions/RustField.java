package org.openzen.zenscript.rustsource.definitions;

import org.openzen.zenscript.rustsource.types.RustType;

public class RustField {
	public final RustFile file;
	public final String name;
	public final String type;
	public final boolean isPublic;

	public RustField(RustFile file, String name, String type, boolean isPublic) {
		this.file = file;
		this.name = name;
		this.type = type;
		this.isPublic = isPublic;
	}
}
