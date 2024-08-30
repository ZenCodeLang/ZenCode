package org.openzen.zenscript.codemodel.generic;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public interface TypeParameterBound {
	boolean isObjectType();

	<T> T accept(GenericParameterBoundVisitor<T> visitor);

	<C, R> R accept(C context, GenericParameterBoundVisitorWithContext<C, R> visitor);

	Optional<ResolvedType> resolveMembers();

	boolean matches(TypeID type);

	TypeParameterBound instance(GenericMapper mapper);

	Optional<TypeID> asType();

	String getCanonical();
}
