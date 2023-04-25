package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.ssa.SSA;
import org.openzen.zenscript.codemodel.statement.Statement;

public class ParsedStatementsFunctionBody implements ParsedFunctionBody {
	private final ParsedStatement body;

	public ParsedStatementsFunctionBody(ParsedStatement body) {
		this.body = body;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		CodeBlock initialBlock = new CodeBlock();
		CompilingStatement compiling = body.compile(compiler, initialBlock);

		SSA ssa = new SSA(initialBlock);
		ssa.compute();

		return compiling.complete();
	}
}
