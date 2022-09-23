package org.openzen.scriptingenginetester;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Optional;

public class TestDiscoverer implements AutoCloseable{

	private FileSystem fileSystem;


	public Path findTestRoot() {
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		return Optional.ofNullable(classLoader.getResource("zencode-tests"))
				.flatMap(TestDiscoverer::urlToUri)
				.map(this::initializeFileSystem)
				.orElseThrow(() -> new IllegalStateException("Could not get zencode-tests root"));
	}

	private static Optional<URI> urlToUri(URL url) {
		try {
			return Optional.of(url.toURI());
		} catch (Exception ignored) {
			return Optional.empty();
		}
	}

	private Path initializeFileSystem(URI uri) {
		if(uri.getScheme().equals("file")) {
			return FileSystems.getFileSystem(uri.resolve("/")).getPath(Paths.get(uri).toString());
		}

		if (uri.getScheme().equals("jar")){

			try {
				fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
				return fileSystem.getPath(uri.getRawSchemeSpecificPart().split("!/")[1]);
			} catch (Exception ex) {
				throw new IllegalStateException("Could not find for " + uri, ex);
			}
		} else {
			throw new IllegalArgumentException("Unsupported schema: " + uri);
		}
	}

	@Override
	public void close() throws IOException {
		if(fileSystem != null) {
			fileSystem.close();
			fileSystem = null;
		}
	}
}
