package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;

public class SubclassResolvingType implements ResolvingType {
	private final ResolvingType superclass;
	private final ResolvingType base;

	public SubclassResolvingType(ResolvingType base, TypeID superclass) {
		this.superclass = superclass.resolve();
		this.base = base;
	}

	@Override
	public TypeID getType() {
		return base.getType();
	}

	@Override
	public ResolvedType withExpansions(List<ExpansionSymbol> expansions) {
		return SubtypeResolvedType.ofChildClass(base.withExpansions(expansions), superclass.withExpansions(expansions));
	}
}
