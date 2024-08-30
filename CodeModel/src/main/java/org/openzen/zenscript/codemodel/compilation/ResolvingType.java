package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;

public interface ResolvingType {
	TypeID getType();

	ResolvedType withExpansions(List<ExpansionSymbol> expansions);
}
