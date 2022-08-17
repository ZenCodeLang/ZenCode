package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedArrayType implements IParsedType {
	public final IParsedType elementType;
	public final int dimension;

	public ParsedArrayType(IParsedType elementType, int dimension) {
		this.elementType = elementType;
		this.dimension = dimension;
	}

	@Override
	public TypeID compile(TypeBuilder typeBuilder) {
		TypeID elementType = this.elementType.compile(typeBuilder);
		return typeBuilder.arrayOf(elementType, dimension);
	}
}
