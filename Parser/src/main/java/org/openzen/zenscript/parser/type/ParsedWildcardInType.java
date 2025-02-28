package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedWildcardInType implements IParsedType {
	private final IParsedType upperBound;

	public ParsedWildcardInType(IParsedType upperBound) {
		this.upperBound = upperBound;
	}

	@Override
	public TypeID compile(TypeBuilder typeBuilder) {
		TypeID upperBound = this.upperBound.compile(typeBuilder);
		return typeBuilder.wildcardIn(upperBound);
	}
}
