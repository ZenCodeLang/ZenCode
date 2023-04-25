package org.openzen.zenscript.codemodel.compilation.statement;

import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;

public abstract class AbstractCompilingStatement implements CompilingStatement {
	protected final StatementCompiler compiler;
	protected final CodeBlock tail;

	public AbstractCompilingStatement(StatementCompiler compiler, CodeBlock tail) {
		this.compiler = compiler;
		this.tail = tail;
	}

	@Override
	public CodeBlock getTail() {
		return tail;
	}
}
