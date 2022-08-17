package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedMapType implements IParsedType {
	public final IParsedType key;
	public final IParsedType value;

	public ParsedMapType(IParsedType key, IParsedType value) {
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
