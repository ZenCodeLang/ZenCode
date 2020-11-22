package org.openzen.zenscript.parser.definitions;

import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.generic.TypeParameterBound;

public abstract class ParsedGenericBound {
	public abstract TypeParameterBound compile(TypeResolutionContext context);
}
