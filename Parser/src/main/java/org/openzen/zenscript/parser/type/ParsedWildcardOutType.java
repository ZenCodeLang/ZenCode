package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedWildcardOutType implements IParsedType {
	private final IParsedType lowerBound;

	public ParsedWildcardOutType(IParsedType lowerBound) {
		this.lowerBound = lowerBound;
	}

	@Override
	public TypeID compile(TypeBuilder typeBuilder) {
		TypeID lowerBound = this.lowerBound.compile(typeBuilder);
		return typeBuilder.wildcardOut(lowerBound);
	}
}
