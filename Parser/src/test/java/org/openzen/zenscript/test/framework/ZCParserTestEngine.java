package org.openzen.zenscript.test.framework;

import org.junit.platform.engine.*;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.descriptor.FileSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ZCParserTestEngine implements TestEngine {
	private static final String ENGINE_ID = "zc-parser-engine";

	@Override
	public String getId() {
		return ENGINE_ID;
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, "ZC Parser Tests");

		Path testDir = Paths.get("src/test/zencode");
		try (Stream<Path> paths = Files.walk(testDir)) {
			paths.filter(path -> path.toString().endsWith(".zc"))
					.forEach(path -> {
						UniqueId testId = uniqueId.append("test", path.toString());
						ParseTestDescriptor testDescriptor = new ParseTestDescriptor(testId, testDir.relativize(path).toString(), path, path.resolveSibling(path.getFileName().toString().replace(".zc", ".zast")));
						engineDescriptor.addChild(testDescriptor);
					});
		} catch (Exception e) {
			System.err.println("Error discovering .zc files: " + e.getMessage());
		}


		return engineDescriptor;
	}

	@Override
	public void execute(ExecutionRequest request) {
		TestDescriptor root = request.getRootTestDescriptor();
		EngineExecutionListener listener = request.getEngineExecutionListener();

		listener.executionStarted(root);

		// Execute all test descriptors
		root.getChildren().stream()
				.filter(testDescriptor -> testDescriptor instanceof ParseTestDescriptor)
				.map(testDescriptor -> (ParseTestDescriptor) testDescriptor)
				.forEach(descriptor -> executeTest(descriptor, listener));

		listener.executionFinished(root, TestExecutionResult.successful());
	}

	private void executeTest(ParseTestDescriptor descriptor, EngineExecutionListener listener) {
		listener.executionStarted(descriptor);

		ParsingTestCase testCase = new ParsingTestCase(descriptor.zcFile(), descriptor.zastFile());
		try {
			testCase.execute();
			listener.executionFinished(descriptor, TestExecutionResult.successful());
		} catch (AssertionError | RuntimeException | IOException ex) {
			listener.executionFinished(descriptor, TestExecutionResult.failed(ex));
		}
	}

	static class ParseTestDescriptor extends AbstractTestDescriptor {
		private final Path zcFile;

		private final Path zastFile;

		public ParseTestDescriptor(UniqueId uniqueId, String displayName, Path zcFile, Path zastFile) {
			super(uniqueId, displayName, FileSource.from(zcFile.toFile()));
			this.zcFile = zcFile;
			this.zastFile = zastFile;
		}

		@Override
		public Type getType() {
			return Type.TEST;
		}

		@Override
		public String getLegacyReportingName() {
			return super.getLegacyReportingName();
		}

		public Path zcFile() {
			return zcFile;
		}

		public Path zastFile() {
			return zastFile;
		}
	}
}