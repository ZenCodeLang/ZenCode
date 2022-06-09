package org.openzen.zenscript.parser.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedTypeRange implements IParsedType {
	private final CodePosition position;
	private final IParsedType from;
	private final IParsedType to;

	public ParsedTypeRange(CodePosition position, IParsedType from, IParsedType to) {
		this.position = position;
		this.from = from;
		this.to = to;
	}

	@Override
	public TypeID compile(TypeBuilder typeBuilder) {
		TypeID from = this.from.compile(typeBuilder);
		TypeID to = this.to.compile(typeBuilder);
		if (from != to)
			return new InvalidTypeID(position, CompileErrors.rangeTypeFromToDifferent());

		return typeBuilder.rangeOf(from);
	}
}
