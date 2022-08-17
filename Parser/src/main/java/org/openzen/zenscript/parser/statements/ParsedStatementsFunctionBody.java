package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.statement.Statement;

public class ParsedStatementsFunctionBody implements ParsedFunctionBody {
	private final ParsedStatement body;

	public ParsedStatementsFunctionBody(ParsedStatement body) {
		this.body = body;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		return body.compile(compiler);
	}
}
