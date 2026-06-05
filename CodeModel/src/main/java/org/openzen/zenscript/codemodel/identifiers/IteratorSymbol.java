package org.openzen.zenscript.codemodel.identifiers;

import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;
import java.util.Optional;

public interface IteratorSymbol extends MethodSymbol {
	enum Kind {
		INT_RANGE,
		ARRAY_VALUES,
		ARRAY_KEY_VALUES,
		ASSOC_KEYS,
		ASSOC_KEY_VALUES,
		STRING_CHARS,
		ITERATOR_VALUES,
		ITERABLE
	}

	Kind getKind();

	TypeID[] getReturnedTypes(TypeID targetType);
}
