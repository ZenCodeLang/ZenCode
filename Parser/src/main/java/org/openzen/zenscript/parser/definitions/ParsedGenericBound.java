package org.openzen.zenscript.parser.definitions;

import org.openzen.zenscript.codemodel.generic.TypeParameterBound;
import org.openzen.zenscript.compiler.TypeBuilder;

public abstract class ParsedGenericBound {
	public abstract TypeParameterBound compile(TypeBuilder typeBuilder);
}
