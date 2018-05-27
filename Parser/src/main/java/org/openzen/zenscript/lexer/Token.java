/* Licensed under GPLv3 - https://opensource.org/licenses/GPL-3.0 */
package org.openzen.zenscript.lexer;

/**
 * Represents a token.
 * 
 * @author Stan Hebben
 * @param <TT> token type
 */
public interface Token<TT extends TokenType>
{
	TT getType();
	
	String getContent();
}
