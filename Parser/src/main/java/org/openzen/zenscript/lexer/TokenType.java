/* Licensed under GPLv3 - https://opensource.org/licenses/GPL-3.0 */
package org.openzen.zenscript.lexer;

/**
 * Represents a token type.
 * 
 * May be a whitespace or non-whitespace token. Whitespace tokens are
 * automatically discarded.
 * 
 * Note: if the whitespace is relevant to the parsing, take care to tag them as
 * non-whitespace, otherwise they will be discarded!
 * 
 * @author Stan Hebben
 */
public interface TokenType
{
	public String getRegexp();
	
	public boolean isWhitespace();
}
