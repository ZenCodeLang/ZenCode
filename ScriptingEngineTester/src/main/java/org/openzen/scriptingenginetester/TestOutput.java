package org.openzen.scriptingenginetester;


import org.openzen.zencode.shared.CompileException;

import java.util.List;

public final class TestOutput {
	public final List<String> output;
	public final List<CompileException> exceptions;
	public final List<Throwable> runtimeExceptions;

	public TestOutput(List<String> output, List<CompileException> exceptions, List<Throwable> runtimeExceptions) {
		this.output = output;
		this.exceptions = exceptions;
		this.runtimeExceptions = runtimeExceptions;
	}
}
