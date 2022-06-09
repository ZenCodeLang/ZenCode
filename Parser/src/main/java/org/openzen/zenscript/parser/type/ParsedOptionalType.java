package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedOptionalType implements IParsedType {
	private final IParsedType type;

	public ParsedOptionalType(IParsedType type) {
		this.type = type;
	}

	@Override
	public TypeID compile(TypeBuilder typeBuilder) {
		TypeID base = type.compile(typeBuilder);
		return typeBuilder.optionalOf(base);
	}
}
