package org.openzen.zenscript.codemodel.identifiers;

import java.util.Optional;

public interface IteratorSymbol {
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

	Optional<MethodSymbol> getMethod();
}
