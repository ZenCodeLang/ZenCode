package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedTypeArray implements IParsedType {
	public final IParsedType baseType;
	public final int dimension;

	public ParsedTypeArray(IParsedType baseType, int dimension) {
		this.baseType = baseType;
		this.dimension = dimension;
	}

	@Override
	public TypeID compile(TypeResolutionContext context) {
		TypeID baseType = this.baseType.compile(context);
		GlobalTypeRegistry registry = context.getTypeRegistry();
		return registry.getArray(baseType, dimension);
	}
}
