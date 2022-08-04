package org.openzen.zenscript.codemodel.serialization;

import org.openzen.zenscript.codemodel.FunctionHeader;

public class StatementSerializationContext {
	private final TypeSerializationContext parent;
	private final FunctionHeader header;

	public StatementSerializationContext(TypeSerializationContext parent, FunctionHeader header) {
		this.parent = parent;
		this.header = header;
	}
}
