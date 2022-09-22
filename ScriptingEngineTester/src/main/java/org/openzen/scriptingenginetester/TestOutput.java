package org.openzen.scriptingenginetester;


import org.openzen.zencode.shared.CompileException;

import java.util.List;

public final class TestOutput {
	public final List<String> output;
	public final List<CompileException> exceptions;

	public TestOutput(List<String> output, List<CompileException> exceptions) {
		this.output = output;
		this.exceptions = exceptions;
	}
}
