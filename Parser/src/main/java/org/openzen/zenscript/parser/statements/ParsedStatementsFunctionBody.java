package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.statement.Statement;

public class ParsedStatementsFunctionBody implements ParsedFunctionBody {
	private final ParsedStatement body;

	public ParsedStatementsFunctionBody(ParsedStatement body) {
		this.body = body;
	}

	@Override
	public Statement compile(StatementCompiler compiler, FunctionHeader header) {
		return body.compile(compiler);
	}
}
