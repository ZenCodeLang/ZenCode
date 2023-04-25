package org.openzen.zenscript.codemodel.compilation.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.statement.InvalidStatement;
import org.openzen.zenscript.codemodel.statement.Statement;

public class InvalidCompilingStatement implements CompilingStatement, CodeBlockStatement {
	private final CodePosition position;
	private final CodeBlock block;
	private final CompileError error;

	public InvalidCompilingStatement(CodePosition position, CodeBlock block, CompileError error) {
		this.position = position;
		this.block = block;
		this.error = error;
		block.add(this);
	}

	@Override
	public Statement complete() {
		return new InvalidStatement(position, error);
	}

	@Override
	public CodeBlock getTail() {
		return block;
	}

	@Override
	public void collect(SSAVariableCollector collector) {}

	@Override
	public void linkVariables(VariableLinker linker) {}
}
