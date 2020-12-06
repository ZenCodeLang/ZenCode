package org.openzen.zenscript.constructor.module.logging;

import org.openzen.zencode.shared.*;

public class EmptyModuleLogger implements ModuleLogger {

	@Override
	public void logCompileException(CompileException exception) {
		error(exception.getMessage());
	}

	@Override
	public void info(String message) {
		System.out.println(message);
	}

	@Override
	public void debug(String message) {
		System.out.println(message);
	}

	@Override
	public void warning(String message) {
		System.out.println(message);
	}

	@Override
	public void error(String message) {
		System.err.println(message);
	}

	@Override
	public void throwingErr(String message, Throwable throwable) {
		System.err.println(message);
		throwable.printStackTrace();
	}

	@Override
	public void throwingWarn(String message, Throwable throwable) {
		System.out.println(message);
		throwable.printStackTrace(System.out);
	}
}
