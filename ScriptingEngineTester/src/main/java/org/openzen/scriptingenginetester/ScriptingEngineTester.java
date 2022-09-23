package org.openzen.scriptingenginetester;

import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.*;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.openzen.scriptingenginetester.cases.TestSuite;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ScriptingEngineTester implements TestEngine, AutoCloseable {
	private final Predicate<Class<?>> IS_TESTABLE_ENGINE = TestableScriptingEngine.class::isAssignableFrom;

	private final TestDiscoverer testDiscoverer = new TestDiscoverer();

	@Override
	public String getId() {
		return "zencode-enginetester";
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest request, UniqueId uniqueId) {
		TestDescriptor engineDescriptor = new EngineDescriptor(uniqueId, "ZenCode scripting engine test");



			final Path testRoot = testDiscoverer.findTestRoot();
			TestSuite suite = new TestSuite(testRoot);

			request.getSelectorsByType(ClasspathRootSelector.class).forEach(selector -> {
				appendTestsInClasspathRoot(suite, selector.getClasspathRoot(), engineDescriptor);
			});

			request.getSelectorsByType(PackageSelector.class).forEach(selector -> {
				appendTestsInPackage(suite, selector.getPackageName(), engineDescriptor);
			});

			request.getSelectorsByType(ClassSelector.class).forEach(selector -> {
				appendTestsInClass(suite, selector.getJavaClass(), engineDescriptor);
			});

			request.getSelectorsByType(UniqueIdSelector.class).forEach(selector -> {
				appendTestsByUid(suite, selector.getUniqueId(), engineDescriptor);
			});


		return engineDescriptor;
	}

	private void appendTestsInClasspathRoot(TestSuite suite, URI uri, TestDescriptor engineDescriptor) {
		ReflectionSupport.findAllClassesInClasspathRoot(uri, IS_TESTABLE_ENGINE, name -> true) //
				.stream() //
				.map(aClass -> new TestSuiteDescriptor((Class<? extends TestableScriptingEngine>)aClass, engineDescriptor, suite)) //
				.forEach(engineDescriptor::addChild);
	}

	private void appendTestsInPackage(TestSuite suite, String packageName, TestDescriptor engineDescriptor) {
		ReflectionSupport.findAllClassesInPackage(packageName, IS_TESTABLE_ENGINE, name -> true) //
				.stream() //
				.map(aClass -> new TestSuiteDescriptor((Class<? extends TestableScriptingEngine>)aClass, engineDescriptor, suite)) //
				.forEach(engineDescriptor::addChild);
	}

	private void appendTestsInClass(TestSuite suite, Class<?> javaClass, TestDescriptor engineDescriptor) {
		if (TestableScriptingEngine.class.isAssignableFrom(javaClass)) {
			engineDescriptor.addChild(new TestSuiteDescriptor((Class<? extends TestableScriptingEngine>)javaClass, engineDescriptor, suite));
		}
	}

	private void appendTestsByUid(TestSuite suite, UniqueId uniqueId, TestDescriptor engineDescriptor) {

		// If we have multiple engines running, we only want ones matching the engine
		// If we don't have an engine set, then we also return
		final Optional<String> engineId = uniqueId.getEngineId();
		if(!engineId.isPresent() || !engineId.get().equals(getId())) {
			return;
		}

		final List<UniqueId.Segment> segments = uniqueId.getSegments();

		try {
			Class<?> javaClass = Class.forName(segments.get(1).getValue());
			if (!TestableScriptingEngine.class.isAssignableFrom(javaClass)) {
				throw new IllegalArgumentException("Invalid Engine class not assignable to TestableScriptingEngine: " + javaClass.getCanonicalName());
			}

			final TestSuiteDescriptor descriptor = new TestSuiteDescriptor((Class<? extends TestableScriptingEngine>) javaClass, engineDescriptor, suite);


			// ToDo: Should an invalid test name throw or just return an empty set?
			//  For empty set, remove this findBy().orElseThrow call.
			descriptor.findByUniqueId(uniqueId)
					.orElseThrow(() -> new IllegalArgumentException("Unknown Test: " + uniqueId));

			// Let's remove all found tests, except the one we matched against
			// Since we atm only have 2 levels of hierarchy, this works, if we need deeper levels, this must be adapted!
			removeChildrenThatAreNotPrefixOf(descriptor, uniqueId);
			descriptor.getChildren().forEach(child -> removeChildrenThatAreNotPrefixOf(child, uniqueId));

			engineDescriptor.addChild(descriptor);

		} catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("Could not find engine class: " + segments.get(1).getValue(), ex);
		}
	}

	@Override
	public void execute(ExecutionRequest request) {
		TestDescriptor root = request.getRootTestDescriptor();

		root.getChildren().forEach(child -> {
			if (child instanceof TestSuiteDescriptor) {
				new ScriptingEngineTestExecutor().execute(request, (TestSuiteDescriptor) child);
			}
		});
	}

	private static void removeChildrenThatAreNotPrefixOf(TestDescriptor descriptor, UniqueId toCheck) {
		new ArrayList<>(descriptor.getChildren())
				.stream()
				.filter(child -> !toCheck.hasPrefix(child.getUniqueId()))
				.forEach(descriptor::removeChild);
	}

	@Override
	public void close() throws IOException {
		this.testDiscoverer.close();
	}
}
