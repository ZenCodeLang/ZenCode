package org.openzen.zenscript.codemodel.compilation.impl;

import org.openzen.zenscript.codemodel.compilation.StaticCallable;
import org.openzen.zenscript.codemodel.type.TypeID;

public class BoundStaticCallable implements StaticCallable {
	private final StaticCallable base;
	private final TypeID[] typeArguments;

	public BoundStaticCallable(StaticCallable base, TypeID[] typeArguments) {
		this.base = base;
		this.typeArguments = typeArguments;
	}
}
