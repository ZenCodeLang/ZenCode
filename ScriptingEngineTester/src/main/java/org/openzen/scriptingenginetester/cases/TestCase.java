package org.openzen.scriptingenginetester.cases;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.UriSource;
import org.openzen.scriptingenginetester.TestOutput;
import org.openzen.zencode.shared.PathSourceFile;
import org.openzen.zencode.shared.SourceFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestCase {
	private final TestSource source;
	private final String name;
	private final List<SourceFile> sourceFiles = new ArrayList<>();
	private final TestAnnotations annotations;

	public TestCase(Path testGroupPath, Path testCasePath) throws IOException {
		if (!testCasePath.startsWith(testGroupPath)) {
			throw new IllegalStateException("Test case is not a part of its group");
		}

		this.source = UriSource.from(testCasePath.toUri());
		this.name = withoutExtension(testCasePath);
		this.sourceFiles.addAll(gatherSourceFiles(testGroupPath, testCasePath));
		assert !this.sourceFiles.isEmpty();
		// We guarantee that sourceFiles.get(0) always exists and that it is the main file
		this.annotations = TestAnnotations.extractFrom(this.sourceFiles.get(0));
	}

	public String getName() {
		return name;
	}

	public List<String> getRequiredStdLibModules() {
		return annotations.getDependencies();
	}

	public void validate(TestOutput output) {
		annotations.getAssertions().validate(output);
	}

    public List<SourceFile> getSourceFiles() {
		return sourceFiles;
    }

	public TestSource getSource() {
		return source;
	}

	private static List<SourceFile> gatherSourceFiles(Path testGroupPath, Path testCasePath) throws IOException {
		if (Files.isDirectory(testCasePath)) {
			Path main = testCasePath.resolve("main.zc");
			if (Files.notExists(main)) {
				throw new IllegalStateException("Main file does not exist in " + testCasePath);
			}

			Stream<Path> contents = Files.walk(testCasePath).filter(it -> !it.equals(testCasePath) && !it.equals(main));
			return Stream.concat(Stream.of(main), Stream.of(contents).flatMap(Function.identity()))
					.map(it -> new PathSourceFile(getFileName(testGroupPath, it, false), it))
					.collect(Collectors.toList());
		} else if (Files.isRegularFile(testCasePath)) {
			return Collections.singletonList(new PathSourceFile(getFileName(testGroupPath, testCasePath, true), testCasePath));
		} else {
			throw new IllegalArgumentException("Unknown file format for " + testCasePath);
		}
	}

	private static String getFileName(Path testGroupPath, Path testCasePath, boolean single) {
		// TODO Make every file test case in its own package?
		Path groupName = testGroupPath.getFileName();
		Path caseName = testGroupPath.relativize(testCasePath);
		return groupName.resolve(caseName).toString();
	}

	private static String withoutExtension(Path testCasePath) {
		String filename = testCasePath.getFileName().toString();
		if(filename.endsWith(".disabled")) {
			filename = filename.substring(0, filename.length() - ".disabled".length());
		}

		int index = filename.lastIndexOf('.');
		return index <= 0 ? filename : filename.substring(0, index);
	}

	public Optional<String> getDisabledReason() {
		Optional<String> disabledReasonFromAnnotation = annotations.getDisabledReason();
		if(disabledReasonFromAnnotation.isPresent()) {
			return disabledReasonFromAnnotation;
		}

		if (getSourceFiles().stream().anyMatch(sourceFile -> sourceFile.getFilename().endsWith(".disabled"))) {
			return Optional.of("Some of the source files were disabled (filename ended with .disabled)");
		}

		return Optional.empty();
	}
}
