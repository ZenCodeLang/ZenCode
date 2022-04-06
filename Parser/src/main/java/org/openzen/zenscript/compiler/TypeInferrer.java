package org.openzen.zenscript.compiler;

import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.types.ResolvedType;

import java.util.Optional;

public interface TypeInferrer {
	Optional<TypeID> union(TypeID left, TypeID right);

	boolean isImplicitCastPossible(TypeID source, TypeID target);

	TypeBuilder types();

	ResolvedType resolve(TypeID type);
}
