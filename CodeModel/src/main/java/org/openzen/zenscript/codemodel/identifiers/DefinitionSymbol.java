package org.openzen.zenscript.codemodel.identifiers;

import org.openzen.zenscript.codemodel.generic.TypeParameter;

import java.util.Optional;

public interface DefinitionSymbol {
	ModuleSymbol getModule();

	String describe();

	boolean isInterface();

	boolean isExpansion();

	TypeParameter[] getTypeParameters();

	Optional<TypeSymbol> asType();

}
