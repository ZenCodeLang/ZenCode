package org.openzen.zenscript.lexer;

import org.openzen.zencode.shared.CodePosition;

public interface TokenStream<TT extends TokenType, T extends Token<TT>> {
	T next() throws ParseException;

	CodePosition getPosition();

	TT getEOF();
}
