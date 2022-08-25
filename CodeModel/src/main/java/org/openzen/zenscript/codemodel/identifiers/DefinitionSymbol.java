package org.openzen.zenscript.codemodel.identifiers;

import org.openzen.zenscript.codemodel.Module;

import java.util.Optional;

public interface DefinitionSymbol {
	Module getModule();

	String describe();

	boolean isInterface();

	boolean isExpansion();

	Optional<TypeSymbol> asType();
}
