package org.openzen.zenscript.codemodel.compilation;

public enum TypeMatch {
	EXACT,
	IMPLICIT,
	NONE;

	public boolean isOk() {
		return this != NONE;
	}

	public static TypeMatch min(TypeMatch a, TypeMatch b) {
		if (a == NONE || b == NONE)
			return NONE;
		else if (a == IMPLICIT || b == IMPLICIT)
			return IMPLICIT;
		else
			return EXACT;
	}

	public static TypeMatch max(TypeMatch a, TypeMatch b) {
		if (a == EXACT || b == EXACT)
			return EXACT;
		else if (a == IMPLICIT || b == IMPLICIT)
			return IMPLICIT;
		else
			return NONE;
	}
}
