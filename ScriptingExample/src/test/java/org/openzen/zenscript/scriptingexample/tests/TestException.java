package org.openzen.zenscript.scriptingexample.tests;

import org.openzen.zencode.java.ZenCodeType;

@ZenCodeType.Name("testsupport.TestException")
public class TestException extends Exception {

	@ZenCodeType.Constructor
	public TestException(String message) {
		super(message);
	}

	@Override
	@ZenCodeType.Getter("message")
	public String getMessage() {
		return super.getMessage();
	}
}
