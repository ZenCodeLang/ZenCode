package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.TypeBuilder;

public class ParsedTypeAssociative implements IParsedType {
	public final IParsedType key;
	public final IParsedType value;

	public ParsedTypeAssociative(IParsedType key, IParsedType value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public TypeID compile(TypeBuilder typeBuilder) {
		TypeID key = this.key.compile(typeBuilder);
		TypeID value = this.value.compile(typeBuilder);
		return typeBuilder.associativeOf(key, value);
	}
}
