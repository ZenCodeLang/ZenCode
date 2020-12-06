package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.statement.Statement;

public class ParsedStatementsFunctionBody implements ParsedFunctionBody {
	private final ParsedStatement body;

	public ParsedStatementsFunctionBody(ParsedStatement body) {
		this.body = body;
	}

	@Override
	public Statement compile(StatementScope scope, FunctionHeader header) {
		return body.compile(scope);
	}
}
