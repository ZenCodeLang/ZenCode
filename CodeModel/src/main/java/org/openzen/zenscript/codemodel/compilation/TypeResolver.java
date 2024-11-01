package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;

public interface TypeResolver {
	List<ExpansionSymbol> getAvailableExpansions();

	ResolvedType resolve(TypeID type);
}
