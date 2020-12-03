package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedOptionalType implements IParsedType {
	private final IParsedType type;
	
	public ParsedOptionalType(IParsedType type) {
		this.type = type;
	}

	@Override
	public TypeID compile(TypeResolutionContext context) {
		TypeID base = type.compile(context);
		return context.getTypeRegistry().getOptional(base);
	}
}
