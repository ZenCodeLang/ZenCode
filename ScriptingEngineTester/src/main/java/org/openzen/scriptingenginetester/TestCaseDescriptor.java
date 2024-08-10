package org.openzen.scriptingenginetester;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.openzen.scriptingenginetester.cases.TestCase;

import java.util.Optional;

public class TestCaseDescriptor extends AbstractTestDescriptor {
	private final TestCase case_;

	public TestCaseDescriptor(TestDescriptor parent, TestCase case_) {
		super(
				parent.getUniqueId().append("class", case_.getName()),
				case_.getName(),
				case_.getSource());

		this.case_ = case_;
		setParent(parent);
	}

	public TestCase getTest() {
		return case_;
	}

	@Override
	public Type getType() {
		return Type.TEST;
	}

	/**
	 * Returns an empty optional if this test should run.
	 * If this test is disabled, returns an Optional that contains the reason why the test should be skipped
	 */
	public Optional<String> getDisabledReason() {
		return case_.getDisabledReason();
	}
}
