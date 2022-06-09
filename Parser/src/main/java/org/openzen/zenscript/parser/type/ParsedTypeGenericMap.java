package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.parser.definitions.ParsedTypeParameter;

public class ParsedTypeGenericMap implements IParsedType {
	private final ParsedTypeParameter key;
	private final IParsedType value;

	public ParsedTypeGenericMap(ParsedTypeParameter key, IParsedType value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public TypeID compile(TypeBuilder typeBuilder) {
		TypeParameter cKey = key.compiled;

		TypeBuilder.GenericMapTypeBuilder builder = typeBuilder.withGeneric(cKey);
		TypeID valueType = this.value.compile(builder);
		return builder.ofValue(valueType);
	}
}
