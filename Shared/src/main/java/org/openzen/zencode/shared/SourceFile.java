package org.openzen.zencode.shared;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public interface SourceFile {
	String getFilename();

	default List<String> getFilePath() {
		List<String> pathParts = new ArrayList<>();
		Paths.get(getFilename()).forEach(p -> pathParts.add(p.toString()));
		return pathParts;
	}

	Reader open() throws IOException;

	void update(String content) throws IOException;

	default int getOrder() {
		return 0;
	}
}
