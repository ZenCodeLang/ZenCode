package org.openzen.zenscript.lexer;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.SourceFile;

import java.io.IOException;

public class CountingCharReader implements CharReader {
	private final CharReader reader;
	private final SourceFile file;

	private int line;
	private int lineOffset;

	public CountingCharReader(CharReader reader, SourceFile file) {
		this.reader = reader;
		this.file = file;

		line = 1;
		lineOffset = 0;
	}

	public CodePosition getPosition() {
		return new CodePosition(file, line, lineOffset, line, lineOffset);
	}

	@Override
	public int peek() throws IOException {
		return reader.peek();
	}

	@Override
	public int next() throws IOException {
		int ch = reader.next();
		if (ch == -1)
			return ch;

		if (ch == '\n') {
			line++;
			lineOffset = 0;
		} else {
			lineOffset++;
		}
		return ch;
	}
}
