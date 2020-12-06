package org.openzen.zenscript.ide.host.local.logging;

import org.openzen.zencode.shared.*;
import org.openzen.zenscript.constructor.module.logging.*;
import org.openzen.zenscript.ide.host.*;
import org.openzen.zenscript.ide.host.local.*;
import org.openzen.zenscript.ide.ui.view.output.*;
import org.openzen.zenscript.validator.*;
import org.openzen.zenscript.validator.logger.*;
import stdlib.*;

import java.util.function.*;

public class LocalModuleLogger implements ModuleLogger, ValidatorLogger {

	private final LocalCompileState state;
	private final Consumer<OutputLine> output;

	public LocalModuleLogger(LocalCompileState state, Consumer<OutputLine> output) {
		this.state = state;
		this.output = output;
	}

	@Override
	public void logCompileException(CompileException exception) {
		IDESourceFile sourceFile = new LocalSourceFile(exception.position.file);
		state.addError(sourceFile, new IDECodeError(sourceFile, exception.position, exception.message));

		String[] lines = Strings.split(exception.getMessage(), '\n');
		for (String line : lines) {
			output.accept(new OutputLine(new ErrorOutputSpan(line)));
		}
	}

	@Override
	public void info(String message) {
		output.accept(new OutputLine(new BasicOutputSpan(message)));
	}

	@Override
	public void debug(String message) {
		output.accept(new OutputLine(new BasicOutputSpan(message)));
	}

	@Override
	public void warning(String message) {
		output.accept(new OutputLine(new BasicOutputSpan(message)));
	}

	@Override
	public void error(String message) {
		output.accept(new OutputLine(new ErrorOutputSpan(message)));
	}

	@Override
	public void throwingErr(String message, Throwable throwable) {
		output.accept(new OutputLine(new ErrorOutputSpan(message)));
	}

	@Override
	public void throwingWarn(String message, Throwable throwable) {
		output.accept(new OutputLine(new BasicOutputSpan(message)));
	}

	@Override
	public void logValidationError(ValidationLogEntry entry) {
		String[] message = Strings.split(entry.message, '\n');
		output.accept(new OutputLine(new ErrorOutputSpan(entry.kind + " " + entry.position.toString() + ": " + message[0])));
		for (int i = 1; i < message.length; i++)
			output.accept(new OutputLine(new ErrorOutputSpan("    " + message[i])));

		IDESourceFile sourceFile = new LocalSourceFile(entry.position.file);
		state.addError(sourceFile, new IDECodeError(sourceFile, entry.position, entry.message));
	}

	@Override
	public void logValidationWarning(ValidationLogEntry warningEntry) {
		logValidationError(warningEntry);
	}
}
