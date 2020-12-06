package org.openzen.zenscript.codemodel;

/**
 * Used to indicate comparison types.
 */
public enum CompareType {
	LT("<"),
	GT(">"),
	EQ("=="),
	NE("!="),
	LE("<="),
	GE(">=");

	public final String str;

	CompareType(String str) {
		this.str = str;
	}
}
