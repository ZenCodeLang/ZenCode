package org.openzen.zenscript.parser.definitions;

import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.generic.ParameterSuperBound;
import org.openzen.zenscript.codemodel.generic.TypeParameterBound;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedSuperBound extends ParsedGenericBound {
	public final IParsedType type;

	public ParsedSuperBound(IParsedType type) {
		this.type = type;
	}

	@Override
	public TypeParameterBound compile(TypeResolutionContext context) {
		return new ParameterSuperBound(type.compile(context));
	}
}
