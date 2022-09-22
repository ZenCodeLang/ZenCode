package org.openzen.scriptingenginetester.cases;

import org.openzen.zencode.shared.CompileExceptionCode;

public class ExpectedError {
	public final String filename;
	public final int line;
	public final CompileExceptionCode error;

	public ExpectedError(String filename, int line, CompileExceptionCode error) {
		this.filename = filename;
		this.line = line;
		this.error = error;
	}
}
