package org.openzen.zencode.shared;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

public final class VirtualSourceFile implements SourceFile {
	public final String filename;

	public VirtualSourceFile(String filename) {
		this.filename = filename;
	}

	@Override
	public Reader open() throws IOException {
		throw new AssertionError("Cannot open virtual source files");
	}

	@Override
	public void update(String content) throws IOException {
		throw new AssertionError("Cannot write to virtual source files");
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
