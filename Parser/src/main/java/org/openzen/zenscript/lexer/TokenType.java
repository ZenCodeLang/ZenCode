package org.openzen.zenscript.lexer;

/**
 * Represents a token type.
 * 
 * May be a whitespace or non-whitespace token. Whitespace tokens are
 * automatically discarded.
 * 
 * Note: if the whitespace is relevant to the parsing, take care to tag them as
 * non-whitespace, otherwise they will be discarded!
 */
public interface TokenType
{
	String getRegexp();
	
	boolean isWhitespace();
}
