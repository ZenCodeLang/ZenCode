package org.openzen.zenscript.codemodel.identifiers;

import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;
import java.util.Optional;

public interface ExpansionSymbol extends DefinitionSymbol {
	Optional<ResolvedType> resolve(TypeID expandingType, List<ExpansionSymbol> expansions);
}
