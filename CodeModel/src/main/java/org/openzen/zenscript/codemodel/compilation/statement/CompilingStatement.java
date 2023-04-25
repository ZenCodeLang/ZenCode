package org.openzen.zenscript.codemodel.compilation.statement;

import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.statement.Statement;

public interface CompilingStatement {
	Statement complete();

	CodeBlock getTail();
}
