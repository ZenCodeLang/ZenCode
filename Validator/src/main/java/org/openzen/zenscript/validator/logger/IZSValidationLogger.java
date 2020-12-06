package org.openzen.zenscript.validator.logger;

import org.openzen.zenscript.validator.ValidationLogEntry;

public interface IZSValidationLogger {

	default void logValidationLogEntry(ValidationLogEntry entry) {
		switch (entry.kind) {
			case ERROR:
				logValidationError(entry);
				break;
			case WARNING:
				logValidationWarning(entry);
				break;
		}
	}

	void logValidationError(ValidationLogEntry errorEntry);

	void logValidationWarning(ValidationLogEntry warningEntry);
}
