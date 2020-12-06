package org.openzen.zencode.java.logger;

import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.validator.ValidationLogEntry;

import java.io.PrintStream;

public class ScriptingEngineStreamLogger implements ScriptingEngineLogger {

	private final PrintStream infoStream, debugStream, warningStream, errorStream, traceStream;

	public ScriptingEngineStreamLogger(PrintStream traceStream, PrintStream debugStream, PrintStream infoStream, PrintStream warningStream, PrintStream errorStream) {
		this.infoStream = infoStream;
		this.debugStream = debugStream;
		this.warningStream = warningStream;
		this.errorStream = errorStream;
		this.traceStream = traceStream;
	}

	public ScriptingEngineStreamLogger(PrintStream normalStream, PrintStream errorStream) {
		this(normalStream, normalStream, normalStream, normalStream, errorStream);
	}

	public ScriptingEngineStreamLogger() {
		this(System.out, System.err);
	}

	@Override
	public void info(String message) {
		infoStream.println("INFO: " + message);
		infoStream.flush();
	}

	@Override
	public void debug(String message) {
		debugStream.println("DEBUG:   " + message);
		debugStream.flush();
	}

	@Override
	public void warning(String message) {
		warningStream.println("WARNING: " + message);
		warningStream.flush();
	}

	@Override
	public void error(String message) {
		errorStream.println("ERROR:   " + message);
	}

	@Override
	public void throwingErr(String message, Throwable throwable) {
		errorStream.println("ERROR:   " + message);
		throwable.printStackTrace(errorStream);
		errorStream.flush();
	}

	@Override
	public void throwingWarn(String message, Throwable throwable) {
		warningStream.println("WARNING: " + message);
		throwable.printStackTrace(warningStream);
		warningStream.flush();
	}

	@Override
	public void trace(String message) {
		traceStream.println("TRACE: " + message);
		traceStream.flush();
	}

	@Override
	public void logCompileException(CompileException exception) {
		throwingErr("Compile Exception:", exception);
	}

	@Override
	public void logSourceFile(SourceFile file) {
		info("Loading File: " + file.getFilename());
	}

	@Override
	public void logValidationError(ValidationLogEntry errorEntry) {
		error(errorEntry.toString());
	}

	@Override
	public void logValidationWarning(ValidationLogEntry warningEntry) {
		warning(warningEntry.toString());
	}
}
