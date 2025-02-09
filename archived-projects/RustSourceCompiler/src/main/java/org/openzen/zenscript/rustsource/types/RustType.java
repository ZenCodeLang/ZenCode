package org.openzen.zenscript.rustsource.types;

import org.openzen.zenscript.rustsource.compiler.ImportSet;

public interface RustType {
	String compile(ImportSet imports);
}
