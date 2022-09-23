package org.openzen.scriptingenginetester;

import org.openzen.scriptingenginetester.cases.TestSuite;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.zip.ZipFile;

public class TestDiscoverer {
	public TestSuite loadTests() {
		final ClassLoader classLoader = TestDiscoverer.class.getClassLoader();
		return Optional.ofNullable(classLoader.getResource("zencode-tests"))
				.flatMap(TestDiscoverer::loadFromURL)
				.orElseThrow(() -> new IllegalArgumentException("Could not find zencode-tests root"));
	}

	private static Optional<TestSuite> loadFromURL(URL url) {
		try {
			if (url.getProtocol().equals("jar")) {
				String path = url.getPath()
						.substring(0, url.getPath().indexOf('!'))
						.replace("file:/", "");
				return Optional.of(new TestSuite(new ZipFile(new File(path)), "zencode-tests"));
			} else {
				return Optional.of(new TestSuite(new File(url.toURI())));
			}
		} catch (Exception ignored) {
			return Optional.empty();
		}
	}
}
