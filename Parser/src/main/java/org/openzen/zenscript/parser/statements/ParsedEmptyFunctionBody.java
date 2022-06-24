package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.statement.Statement;

public class ParsedEmptyFunctionBody implements ParsedFunctionBody {
	public final CodePosition position;

	public ParsedEmptyFunctionBody(CodePosition position) {
		this.position = position;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		return null;
	}
}
