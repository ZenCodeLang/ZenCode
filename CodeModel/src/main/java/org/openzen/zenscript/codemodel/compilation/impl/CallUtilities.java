package org.openzen.zenscript.codemodel.compilation.impl;

import org.openzen.zenscript.codemodel.compilation.AnyMethod;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.type.TypeID;

public class CallUtilities {
	private CallUtilities() {}

	public static CallArguments match(AnyMethod method, CompilingExpression... arguments) {
		// TODO: going to be an interesting job
	}

	public static CallArguments match(AnyMethod method, TypeID result, CompilingExpression... arguments) {
		// TODO: going to be an interesting job
	}
}
