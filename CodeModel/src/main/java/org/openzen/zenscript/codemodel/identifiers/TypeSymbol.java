package org.openzen.zenscript.codemodel.identifiers;

import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

/**
 * Uniquely identifies a type and allows a limited set of information to be retrieved from it.
 */
public interface TypeSymbol extends DefinitionSymbol {
	Modifiers getModifiers();

	boolean isStatic();

	boolean isEnum();

	String getName();

	ResolvingType resolve(TypeID type, TypeID[] typeArguments);

	Optional<TypeSymbol> getOuter();

	Optional<TypeID> getSupertype(TypeID[] typeArguments);

	default TypeID normalize(TypeID[] typeArguments) {
		return new DefinitionTypeID(this, typeArguments, null);
	}

	@Override
	default Optional<TypeSymbol> asType() {
		return Optional.of(this);
	}
}
