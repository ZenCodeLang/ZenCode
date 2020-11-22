package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.parser.PrecompilationState;

public interface ParsedFunctionBody {
	Statement compile(StatementScope scope, FunctionHeader header);
}
