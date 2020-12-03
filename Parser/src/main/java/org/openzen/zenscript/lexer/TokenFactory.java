package org.openzen.zenscript.lexer;

public interface TokenFactory<T, TT> {
	T create(TT type, String content);
}
