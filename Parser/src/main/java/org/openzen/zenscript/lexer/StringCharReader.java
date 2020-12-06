package org.openzen.zenscript.lexer;

import java.io.IOException;

public class StringCharReader implements CharReader {
	private final char[] data;
	private int index;

	public StringCharReader(String data) {
		this.data = data.toCharArray();
		this.index = 0;
	}

	@Override
	public int peek() {
		return index >= data.length ? -1 : data[index];
	}

	@Override
	public int next() throws IOException {
		return index >= data.length ? -1 : data[index++];
	}
}
