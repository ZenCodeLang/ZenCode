package org.openzen.zenscript.compiler.expression;

public enum TypeMatch {
	EXACT,
	IMPLICIT,
	NONE;

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
