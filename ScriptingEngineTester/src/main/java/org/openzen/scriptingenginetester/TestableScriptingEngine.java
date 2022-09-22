package org.openzen.scriptingenginetester;

import org.openzen.scriptingenginetester.cases.TestCase;

public interface TestableScriptingEngine {
	TestOutput run(TestCase test);
}
