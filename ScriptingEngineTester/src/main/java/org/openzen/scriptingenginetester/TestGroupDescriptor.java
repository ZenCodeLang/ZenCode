package org.openzen.scriptingenginetester;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.FileSource;
import org.openzen.scriptingenginetester.cases.TestCase;
import org.openzen.scriptingenginetester.cases.TestGroup;
import org.openzen.scriptingenginetester.cases.TestSuite;

public class TestGroupDescriptor extends AbstractTestDescriptor {
	private final TestGroup group;

	public TestGroupDescriptor(TestDescriptor parent, TestGroup group) {
		super(
				parent.getUniqueId().append("class", group.getName()),
				group.getName(),
				group.getSource());

		this.group = group;
		setParent(parent);
		addAllChildren();
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	private void addAllChildren() {
		for (TestCase case_ : group.getCases()) {
			addChild(new TestCaseDescriptor(this, case_));
		}
	}
}
