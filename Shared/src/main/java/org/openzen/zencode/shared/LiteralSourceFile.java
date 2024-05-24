package org.openzen.zencode.shared;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

public final class LiteralSourceFile implements SourceFile {
	public final String filename;
	private final String contents;

	public LiteralSourceFile(String filename, String contents) {
		this.filename = filename;
		this.contents = contents;
	}

	@Override
	public Reader open() throws IOException {
		return new StringReader(contents);
	}

	@Override
	public void update(String contents) throws IOException {
		throw new AssertionError("Cannot update literal source files");
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public List<String> getFilePath() {
		return Collections.singletonList(filename);
	}
}
