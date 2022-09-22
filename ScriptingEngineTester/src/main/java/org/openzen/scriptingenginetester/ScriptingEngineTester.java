package org.openzen.scriptingenginetester;

import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.*;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.openzen.scriptingenginetester.cases.TestSuite;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.function.Predicate;

public class ScriptingEngineTester implements TestEngine {
	private final Predicate<Class<?>> IS_TESTABLE_ENGINE = TestableScriptingEngine.class::isAssignableFrom;

	@Override
	public String getId() {
		return "zencode-enginetester";
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest request, UniqueId uniqueId) {
		TestDescriptor engineDescriptor = new EngineDescriptor(uniqueId, "ZenCode scripting engine test");

		try {
			TestSuite suite = new TestSuite(new File("C:\\Repositories\\OpenZen\\ZenScript\\ScriptingEngineTester\\tests"));

			request.getSelectorsByType(ClasspathRootSelector.class).forEach(selector -> {
				appendTestsInClasspathRoot(suite, selector.getClasspathRoot(), engineDescriptor);
			});

			request.getSelectorsByType(PackageSelector.class).forEach(selector -> {
				appendTestsInPackage(suite, selector.getPackageName(), engineDescriptor);
			});

			request.getSelectorsByType(ClassSelector.class).forEach(selector -> {
				appendTestsInClass(suite, selector.getJavaClass(), engineDescriptor);
			});
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

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

	@Override
	public void execute(ExecutionRequest request) {
		TestDescriptor root = request.getRootTestDescriptor();

		root.getChildren().forEach(child -> {
			if (child instanceof TestSuiteDescriptor) {
				new ScriptingEngineTestExecutor().execute(request, (TestSuiteDescriptor) child);
			}
		});
	}
}
