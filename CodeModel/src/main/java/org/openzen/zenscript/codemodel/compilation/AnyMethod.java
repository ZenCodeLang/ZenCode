package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public interface AnyMethod {
	FunctionHeader getHeader();

	Optional<MethodInstance> asMethod();

	AnyMethod withGenericArguments(GenericMapper mapper);

	default boolean hasWideningConversions() {
		return false;
	}
}
