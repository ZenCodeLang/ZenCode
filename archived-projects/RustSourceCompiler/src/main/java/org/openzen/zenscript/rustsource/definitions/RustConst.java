package org.openzen.zenscript.rustsource.definitions;

import org.openzen.zenscript.rustsource.expressions.RustExpression;

public class RustConst {
	public final String name;
	public final String value;

	public RustConst(String name, String value) {
		this.name = name;
		this.value = value;
	}
}
