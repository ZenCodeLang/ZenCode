package org.openzen.scriptingenginetester;

import org.junit.jupiter.api.extension.TestInstantiationException;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.openzen.scriptingenginetester.cases.TestGroup;
import org.openzen.scriptingenginetester.cases.TestSuite;

import java.lang.reflect.InvocationTargetException;

public class TestSuiteDescriptor extends AbstractTestDescriptor {
	private final Class<? extends TestableScriptingEngine> testClass;
	private final TestSuite suite;

	public TestSuiteDescriptor(Class<? extends TestableScriptingEngine> testClass, TestDescriptor parent, TestSuite suite) {
		super(
				parent.getUniqueId().append("class", testClass.getName()),
				testClass.getSimpleName(),
				ClassSource.from(testClass));

		setParent(parent);
		this.testClass = testClass;
		this.suite = suite;
		addAllChildren();
	}

	public TestableScriptingEngine instantiate() {
		try {
			return testClass.getConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private void addAllChildren() {
		for (TestGroup group : suite.getGroups()) {
			addChild(new TestGroupDescriptor(this, group));
		}
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}
}
