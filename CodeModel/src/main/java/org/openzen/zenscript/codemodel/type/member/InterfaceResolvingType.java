package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InterfaceResolvingType implements ResolvingType {
	private final ResolvingType baseType;
	private final Collection<TypeID> implementedInterfaces;

	public static ResolvingType of(ResolvingType baseType, Collection<TypeID> implementedInterfaces) {
		if (implementedInterfaces.isEmpty()) {
			return baseType;
		}

		return new InterfaceResolvingType(baseType, implementedInterfaces);
	}

	private InterfaceResolvingType(ResolvingType baseType, Collection<TypeID> implementedInterfaces) {
		this.baseType = baseType;
		this.implementedInterfaces = implementedInterfaces;
	}

	@Override
	public TypeID getType() {
		return baseType.getType();
	}

	@Override
	public ResolvedType withExpansions(List<ExpansionSymbol> expansions) {
		List<ResolvedType> resolvedInterfaces = implementedInterfaces.stream().map(iface -> iface.resolve().withExpansions(expansions)).collect(Collectors.toList());

		List<ResolvedType> interfaceExpansions = implementedInterfaces.stream()
				.flatMap(iface -> expansions.stream().map(expansion -> expansion.resolve(iface)).filter(Optional::isPresent).map(Optional::get))
				.collect(Collectors.toList());

		return SubtypeResolvedType.ofImplementation(
				ExpandedResolvedType.of(
						baseType.withExpansions(expansions),
						interfaceExpansions),
				resolvedInterfaces
		);
	}
}
