package org.openzen.scriptingenginetester;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.FileSource;
import org.openzen.scriptingenginetester.cases.TestCase;

public class TestCaseDescriptor extends AbstractTestDescriptor {
	private final TestCase case_;

	public TestCaseDescriptor(TestDescriptor parent, TestCase case_) {
		super(
				parent.getUniqueId().append("test", case_.getName()),
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
}
