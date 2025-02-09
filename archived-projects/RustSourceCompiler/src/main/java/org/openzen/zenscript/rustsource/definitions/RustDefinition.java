package org.openzen.zenscript.rustsource.definitions;

import org.openzen.zenscript.rustsource.compiler.ImportSet;

public abstract class RustDefinition {
	public final RustFile file;

	public RustDefinition(RustFile file) {
		this.file = file;
	}

	public abstract String compile(ImportSet imports);
}
