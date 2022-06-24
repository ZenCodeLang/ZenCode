package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.statement.Statement;

public interface ParsedFunctionBody {
	Statement compile(StatementCompiler compiler);
}
