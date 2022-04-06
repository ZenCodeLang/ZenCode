package org.openzen.zenscript.compiler.types;

import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.function.Predicate;

public interface MethodInfo {
	boolean isImplicit();

	boolean isCompatible(TypeID returnType, TypeID... argumentTypes);

	boolean isCompatible(TypeID returnType, Predicate<TypeID>... argumentTypes);
}
