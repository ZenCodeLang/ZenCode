package org.openzen.zenscript.ide.host.local.logging;

import org.openzen.zenscript.ide.host.*;
import org.openzen.zenscript.validator.*;
import org.openzen.zenscript.validator.logger.*;

import java.util.function.*;

public class LocalValidatorLogger implements ValidatorLogger {

	private final Consumer<IDECodeError> errors;

	public LocalValidatorLogger(Consumer<IDECodeError> errors) {
		this.errors = errors;
	}

	@Override
	public void info(String message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void debug(String message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void warning(String message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void error(String message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void throwingErr(String message, Throwable throwable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void throwingWarn(String message, Throwable throwable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void logValidationError(ValidationLogEntry errorEntry) {
		logValidationWarning(errorEntry);
	}

	@Override
	public void logValidationWarning(ValidationLogEntry entry) {
		final IDECodeError ideCodeError = new IDECodeError(null, entry.position, entry.message);
		errors.accept(ideCodeError);
	}
}
