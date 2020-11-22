package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedFunctionType implements IParsedType {
	private final ParsedFunctionHeader header;

	public ParsedFunctionType(ParsedFunctionHeader header) {
		this.header = header;
	}

	@Override
	public TypeID compile(TypeResolutionContext context) {
		return context.getTypeRegistry().getFunction(header.compile(context));
	}
}
