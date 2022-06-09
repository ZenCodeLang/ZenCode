package org.openzen.zenscript.parser.definitions;

import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.generic.TypeParameterBound;

public abstract class ParsedGenericBound {
	public abstract TypeParameterBound compile(TypeBuilder typeBuilder);
}
