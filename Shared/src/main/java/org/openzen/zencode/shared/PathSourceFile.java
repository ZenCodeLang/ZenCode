package org.openzen.zencode.shared;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathSourceFile implements SourceFile{
	private final Path path;
	private final String name;

	public PathSourceFile(String name, Path path) {
		this.name = name;
		this.path = path;
	}

	@Override
	public String getFilename() {
		return name;
	}

	@Override
	public Reader open() throws IOException {
		return Files.newBufferedReader(path);
	}

	@Override
	public void update(String content) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			writer.write(content);
		}
	}
}
