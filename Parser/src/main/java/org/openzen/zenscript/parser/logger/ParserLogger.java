package org.openzen.zenscript.parser.logger;

import org.openzen.zencode.shared.logging.CompileExceptionLogger;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.lexer.ParseException;

public interface ParserLogger extends IZSLogger, CompileExceptionLogger {
	default void logParseException(ParseException exception) {
		throwingErr("Parser Exception @ " + exception.position.toString() + " : " + exception.message, exception);
	}
}
