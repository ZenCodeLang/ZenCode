package org.openzen.zenscript.validator.logger;

import org.openzen.zenscript.validator.ValidationLogEntry;

public interface IZSValidationLogger {

	default void logValidationLogEntry(ValidationLogEntry entry) {
		logValidationError(entry);
	}

	void logValidationError(ValidationLogEntry errorEntry);

	void logValidationWarning(ValidationLogEntry warningEntry);
}
