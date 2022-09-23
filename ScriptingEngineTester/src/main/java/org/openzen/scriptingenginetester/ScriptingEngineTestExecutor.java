package org.openzen.scriptingenginetester;

import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

public class ScriptingEngineTestExecutor {
	public void execute(ExecutionRequest request, TestSuiteDescriptor suite) {
		request.getEngineExecutionListener().executionStarted(suite);
		suite.getChildren().forEach(descriptor -> execute(request, suite, descriptor));
		request.getEngineExecutionListener().executionFinished(suite, TestExecutionResult.successful());
	}

	private void execute(ExecutionRequest request, TestSuiteDescriptor suite, TestDescriptor descriptor) {
		if (descriptor instanceof TestGroupDescriptor) {
			executeGroup(request, suite, descriptor);
		}
		if (descriptor instanceof TestCaseDescriptor) {
			executeCase(request, suite.instantiate(), (TestCaseDescriptor) descriptor);
		}
	}

	private void executeGroup(ExecutionRequest request, TestSuiteDescriptor suite, TestDescriptor descriptor) {
		request.getEngineExecutionListener().executionStarted(descriptor);
		descriptor.getChildren().forEach(child -> execute(request, suite, child));
		request.getEngineExecutionListener().executionFinished(descriptor, TestExecutionResult.successful());
	}

	private void executeCase(ExecutionRequest request, TestableScriptingEngine engine, TestCaseDescriptor descriptor) {
		request.getEngineExecutionListener().executionStarted(descriptor);
		try {
			TestOutput output = engine.run(descriptor.getTest());
			descriptor.getTest().validate(output);
			request.getEngineExecutionListener().executionFinished(descriptor, TestExecutionResult.successful());
		} catch (AssertionError|RuntimeException ex) {
			request.getEngineExecutionListener().executionFinished(descriptor, TestExecutionResult.failed(ex));
		}
	}
}
