package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;

public class ParsedFunctionType implements IParsedType {
	private final ParsedFunctionHeader header;

	public ParsedFunctionType(ParsedFunctionHeader header) {
		this.header = header;
	}

	@Override
	public TypeID compile(TypeBuilder typeBuilder) {
		return typeBuilder.functionOf(header.compile(typeBuilder));
	}
}
