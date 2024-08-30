package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;
import java.util.stream.Collectors;

public class ExpandedResolvingType implements ResolvingType {

	private final ResolvingType base;
	private final List<ResolvingType> expansions;

	private ExpandedResolvingType(ResolvingType base, List<ResolvingType> expansions) {
		this.base = base;
		this.expansions = expansions;
	}

	public static ResolvingType of(ResolvingType base, List<ResolvingType> expansions) {
		if (expansions.isEmpty()) {
			return base;
		}

		return new ExpandedResolvingType(base, expansions);
	}

	@Override
	public TypeID getType() {
		return base.getType();
	}

	@Override
	public ResolvedType withExpansions(List<ExpansionSymbol> expansions) {
		List<ResolvedType> newExpansions = this.expansions.stream().map(expansion -> expansion.withExpansions(expansions)).collect(Collectors.toList());
		return ExpandedResolvedType.of(base.withExpansions(expansions), newExpansions);
	}
}
