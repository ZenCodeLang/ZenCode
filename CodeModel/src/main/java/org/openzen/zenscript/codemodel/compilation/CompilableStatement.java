package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.statement.Statement;

public interface CompilableStatement {
	Statement compile(StatementCompiler compiler);
}
