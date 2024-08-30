package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;

public class OptionalResolvingType implements ResolvingType {
	private final OptionalTypeID type;
	private final ResolvingType baseType;

	public OptionalResolvingType(OptionalTypeID type, ResolvingType baseType) {
		this.type = type;
		this.baseType = baseType;
	}

	@Override
	public TypeID getType() {
		return type;
	}

	@Override
	public ResolvedType withExpansions(List<ExpansionSymbol> expansions) {
		OptionalResolvedType baseWithExpansions = new OptionalResolvedType(type, baseType.withExpansions(expansions));
		return ExpandedResolvedType.resolve(baseWithExpansions, expansions);
	}
}
