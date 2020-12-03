package org.openzen.zenscript.compiler;

import org.openzen.zencode.shared.CodePosition;

public interface CompilerOutput {
	void info(String message);
	
	void warning(String message);
	
	void warning(CodePosition position, String message);
	
	void error(String message);
	
	void error(CodePosition position, String message);
}
