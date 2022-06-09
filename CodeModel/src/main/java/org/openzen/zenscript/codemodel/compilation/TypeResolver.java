package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.type.TypeID;

public interface TypeResolver {
	ResolvedType resolve(TypeID type);
}
