package org.openzen.zenscript.scriptingexample.tests.runner;

import org.openzen.scriptingenginetester.TestOutput;
import org.openzen.zencode.java.logger.ScriptingEngineStreamLogger;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.scriptingexample.tests.PrintLogger;
import org.openzen.zenscript.validator.ValidationLogEntry;

import java.util.ArrayList;
import java.util.List;

public class EngineTestLogger extends ScriptingEngineStreamLogger implements PrintLogger {
	private final List<String> printOutput = new ArrayList<>();
	private final List<CompileException> exceptions = new ArrayList<>();

	@Override
	public void logCompileException(CompileException exception) {
		super.logCompileException(exception);
		exceptions.add(exception);
	}

	@Override
	public void logValidationError(ValidationLogEntry errorEntry) {
		super.logValidationError(errorEntry);
		// TODO
	}

	@Override
	public void logValidationWarning(ValidationLogEntry warningEntry) {
		super.logValidationWarning(warningEntry);
		// TODO
	}

	@Override
	public void logPrintln(String line) {
		printOutput.add(line);
		info(line);
	}

	public TestOutput getOutput() {
		return new TestOutput(printOutput, exceptions);
	}
}
