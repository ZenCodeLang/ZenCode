package org.openzen.zencode.shared;

public final class CompileException extends Exception {
	public final CodePosition position;
	public final CompileError error;

	public CompileException(CodePosition position, CompileError error) {
		super(position.toString() + ": [" + error.code.toString() + "] " + error.description);
		this.position = position;
		this.error = error;
	}

	public static CompileException internalError(String message) {
		return new CompileException(CodePosition.BUILTIN, new CompileError(CompileExceptionCode.INTERNAL_ERROR, message));
	}

	public CodePosition getPosition() {
		return position;
	}

	public CompileExceptionCode getCode() {
		return error.code;
	}

	@Override
	public String getMessage() {
		return position.toString() + ": " + error.description;
	}
}
