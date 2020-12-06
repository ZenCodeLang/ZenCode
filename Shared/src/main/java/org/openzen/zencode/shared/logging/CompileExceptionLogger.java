package org.openzen.zencode.shared.logging;

import org.openzen.zencode.shared.CompileException;

public interface CompileExceptionLogger {
	void logCompileException(CompileException exception);
}
