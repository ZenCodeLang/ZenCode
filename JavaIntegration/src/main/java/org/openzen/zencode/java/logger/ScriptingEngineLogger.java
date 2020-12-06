package org.openzen.zencode.java.logger;

import org.openzen.zencode.shared.logging.SourceFileLogger;
import org.openzen.zenscript.parser.logger.ParserLogger;
import org.openzen.zenscript.validator.logger.ValidatorLogger;

public interface ScriptingEngineLogger extends ValidatorLogger, SourceFileLogger, ParserLogger {
}
