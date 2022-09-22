package org.openzen.scriptingenginetester;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;

public class TestDiscoverer {

	public File findTestRoot() {
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		return Optional.ofNullable(classLoader.getResource("zencode-tests"))
				.flatMap(TestDiscoverer::urlToUri)
				.map(File::new)
				.orElseThrow(() -> new IllegalStateException("Could not get zencode-tests root"));
	}

	private static Optional<URI> urlToUri(URL url) {
		try {
			return Optional.of(url.toURI());
		} catch (Exception ignored) {
			return Optional.empty();
		}
	}
}
