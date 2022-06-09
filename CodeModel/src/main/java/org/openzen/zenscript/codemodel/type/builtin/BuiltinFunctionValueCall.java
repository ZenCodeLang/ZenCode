package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;

public class BuiltinFunctionValueCall implements MethodSymbol {
	private final FunctionTypeSymbol type;

	public BuiltinFunctionValueCall(FunctionTypeSymbol type) {
		this.type = type;
	}

	@Override
	public DefinitionSymbol getDefiningType() {
		return type;
	}

	@Override
	public String getName() {
		return "()";
	}

	@Override
	public FunctionHeader getHeader() {
		return type.header;
	}
}
