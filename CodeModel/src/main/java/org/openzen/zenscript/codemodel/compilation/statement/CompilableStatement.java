package org.openzen.zenscript.codemodel.compilation.statement;

import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;

public interface CompilableStatement {
	CompilingStatement compile(StatementCompiler compiler, CodeBlock block);
}
