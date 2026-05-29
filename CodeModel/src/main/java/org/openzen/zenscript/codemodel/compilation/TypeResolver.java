package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;
import java.util.Optional;

public interface TypeResolver {
	List<ExpansionSymbol> getAvailableExpansions();

	ResolvedType resolve(TypeID type);

	default Optional<TypeID> union(TypeID left, TypeID right) {
		if (left.equals(right))
			return Optional.of(right);

		ResolvedType leftResolved = resolve(left);
		ResolvedType rightResolved = resolve(right);

		if (leftResolved.canCastImplicitlyTo(right))
			return Optional.of(right);

		if (rightResolved.canCastImplicitlyTo(left))
			return Optional.of(left);

		Optional<ArrayTypeID> maybeLeftArray = left.asArray();
		Optional<ArrayTypeID> maybeRightArray = right.asArray();
		if (maybeLeftArray.isPresent() && maybeRightArray.isPresent()) {
			ArrayTypeID leftArray = maybeLeftArray.get();
			ArrayTypeID rightArray = maybeRightArray.get();

			if (leftArray.dimension == rightArray.dimension) {
				return union(leftArray.elementType, rightArray.elementType)
						.map(t -> new ArrayTypeID(t, leftArray.dimension));
			}
		}

		return Optional.empty();
	}
}
