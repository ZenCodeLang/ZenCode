package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedTypeAssociative implements IParsedType {
	public final IParsedType key;
	public final IParsedType value;

	public ParsedTypeAssociative(IParsedType key, IParsedType value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public TypeID compile(TypeResolutionContext context) {
		TypeID key = this.key.compile(context);
		TypeID value = this.value.compile(context);
		return context.getTypeRegistry().getAssociative(key, value);
	}
}
