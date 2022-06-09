package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;

import java.util.Optional;

public interface AnyMethod {
	FunctionHeader getHeader();

	Optional<MethodSymbol> asMethod();
}
