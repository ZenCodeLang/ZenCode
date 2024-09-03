package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;

public class SubclassResolvingType implements ResolvingType {
	private final ResolvingType superclass;
	private final ResolvingType base;
	private final TypeID supertype;

	public SubclassResolvingType(ResolvingType superclass, ResolvingType base, TypeID supertype) {
		this.superclass = superclass;
		this.base = base;
		this.supertype = supertype;
	}

	@Override
	public TypeID getType() {
		return base.getType();
	}

	@Override
	public ResolvedType withExpansions(List<ExpansionSymbol> expansions) {
		return new SubclassResolvedType(superclass.withExpansions(expansions), base.withExpansions(expansions), supertype);
	}
}
