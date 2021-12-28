package org.openzen.zenscript.scriptingexample.tests.helpers;

import org.junit.jupiter.api.Assertions;
import org.openzen.zencode.java.logger.ScriptingEngineStreamLogger;

public class ZenCodeTestLogger extends ScriptingEngineStreamLogger {

	private static final boolean logDebug = false;
	private final ZenCodeTestLoggerOutput printlnOutputs;
	private final ZenCodeTestLoggerOutput errors;
	private final ZenCodeTestLoggerOutput warnings;
	private boolean isEngineComplete = false;

	public ZenCodeTestLogger() {
		this.printlnOutputs = new ZenCodeTestLoggerOutput();
		this.errors = new ZenCodeTestLoggerOutput();
		this.warnings = new ZenCodeTestLoggerOutput();
	}

	public ZenCodeTestLogger(ZenCodeTestLoggerOutput printlnOutputs, ZenCodeTestLoggerOutput errors, ZenCodeTestLoggerOutput warnings) {
		this.printlnOutputs = printlnOutputs;
		this.errors = errors;
		this.warnings = warnings;
	}

	@Override
	public void debug(String message) {
		if (logDebug) {
			super.debug(message);
		}
	}

	@Override
	public void warning(String message) {
		warnings().add(message);
		super.warning(message);
	}

	@Override
	public void throwingWarn(String message, Throwable throwable) {
		warnings().add(message);
		super.throwingWarn(message, throwable);
	}

	public void logPrintln(String line) {
		info(line);
		this.printlnOutputs().add(line);
	}

	@Override
	public void error(String message) {
		errors().add(message);
		super.error(message);
	}

	@Override
	public void throwingErr(String message, Throwable throwable) {
		errors().add(message);
		super.throwingErr(message, throwable);
	}

	public void setEngineComplete() {
		isEngineComplete = true;
	}

	public void assertPrintOutput(int line, String content) {
		if (!isEngineComplete) {
			Assertions.fail("Trying to call an assertion before the engine ran, probably a fault in the test!");
		}
		printlnOutputs().assertLine(line, content);
	}

	public void assertPrintOutputSize(int size) {
		if (!isEngineComplete) {
			Assertions.fail("Trying to call an assertion before the engine ran, probably a fault in the test!");
		}
		printlnOutputs().assertSize(size);
	}

	public void assertNoErrors() {
		errors().assertEmpty();
	}

	public void assertNoWarnings() {
		warnings().assertEmpty();
	}

	public ZenCodeTestLoggerOutput printlnOutputs() {
		return printlnOutputs;
	}

	public ZenCodeTestLoggerOutput errors() {
		return errors;
	}

	public ZenCodeTestLoggerOutput warnings() {
		return warnings;
	}

	public boolean isEngineComplete() {
		return isEngineComplete;
	}
}
