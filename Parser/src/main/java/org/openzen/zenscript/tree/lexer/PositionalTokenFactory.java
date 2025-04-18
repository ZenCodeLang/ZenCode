package org.openzen.zenscript.tree.lexer;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.lexer.Token;
import org.openzen.zenscript.lexer.TokenType;

public interface PositionalTokenFactory<TT extends TokenType, T extends Token<TT>> {
	PositionedToken<TT, T> create(CodePosition position, TT type, String content);
}
