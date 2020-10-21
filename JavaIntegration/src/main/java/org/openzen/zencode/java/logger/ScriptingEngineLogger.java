package org.openzen.zencode.java.logger;

import org.openzen.zencode.shared.logging.*;
import org.openzen.zenscript.parser.logger.*;
import org.openzen.zenscript.validator.logger.*;

public interface ScriptingEngineLogger extends ValidatorLogger, SourceFileLogger, ParserLogger {}
