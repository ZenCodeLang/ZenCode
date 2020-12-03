package org.openzen.zenscript.lexer;

/**
 * Represents a token.
 * 
 * @param <TT> token type
 */
public interface Token<TT extends TokenType>
{
	TT getType();
	
	String getContent();
}
