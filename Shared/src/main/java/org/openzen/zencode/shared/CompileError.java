package org.openzen.zencode.shared;

public class CompileError {
	public final CompileExceptionCode code;
	public final String description;

	public CompileError(CompileExceptionCode code, String description) {
		this.code = code;
		this.description = description;
	}
}
