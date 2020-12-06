package org.openzen.zenscript.parser.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
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
	public TypeID compile(TypeResolutionContext context) {
		TypeID from = this.from.compile(context);
		TypeID to = this.to.compile(context);
		if (!from.equals(to))
			return new InvalidTypeID(position, CompileExceptionCode.NO_SUCH_TYPE, "from and to in a range must be the same type");

		return context.getTypeRegistry().getRange(from);
	}
}
