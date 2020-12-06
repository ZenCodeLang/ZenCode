package org.openzen.zenscript.lexer;

import java.io.IOException;

public interface CharReader {
	int peek() throws IOException;

	int next() throws IOException;
}
