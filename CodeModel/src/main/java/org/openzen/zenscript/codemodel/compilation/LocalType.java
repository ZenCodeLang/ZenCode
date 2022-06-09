package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public interface LocalType {
	TypeID getThisType();

	Optional<TypeID> getSuperType();

	StaticCallable thisCall();

	Optional<StaticCallable> superCall();
}
