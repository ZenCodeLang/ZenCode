package org.openzen.zenscript.codemodel.compilation.impl;

import org.openzen.zenscript.codemodel.compilation.InstanceCallable;
import org.openzen.zenscript.codemodel.compilation.StaticCallable;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

public class BoundInstanceCallable implements StaticCallable {
	private final InstanceCallable base;
	private final Expression target;
	private final TypeID[] typeArguments;

	public BoundInstanceCallable(InstanceCallable base, Expression target, TypeID[] typeArguments) {
		this.base = base;
		this.target = target;
		this.typeArguments = typeArguments;
	}
}
