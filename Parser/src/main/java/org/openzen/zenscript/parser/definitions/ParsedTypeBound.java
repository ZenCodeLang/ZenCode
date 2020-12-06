package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameterBound;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedTypeBound extends ParsedGenericBound {
	public final CodePosition position;
	public final IParsedType type;

	public ParsedTypeBound(CodePosition position, IParsedType type) {
		this.position = position;
		this.type = type;
	}

	@Override
	public TypeParameterBound compile(TypeResolutionContext context) {
		return new ParameterTypeBound(position, type.compile(context));
	}
}
