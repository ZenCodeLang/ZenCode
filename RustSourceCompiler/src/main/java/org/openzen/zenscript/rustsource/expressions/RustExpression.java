package org.openzen.zenscript.rustsource.expressions;

import org.openzen.zenscript.rustsource.compiler.ImportSet;

public interface RustExpression {
	String compile(ImportSet imports);
}
