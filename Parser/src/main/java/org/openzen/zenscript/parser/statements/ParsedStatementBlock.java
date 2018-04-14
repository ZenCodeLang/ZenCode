package org.openzen.zenscript.parser.statements;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.linker.BlockScope;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.shared.CodePosition;

public class ParsedStatementBlock extends ParsedStatement {
	private final List<ParsedStatement> statements;

	public ParsedStatementBlock(CodePosition position, List<ParsedStatement> statements) {
		super(position);

		this.statements = statements;
	}

	@Override
	public Statement compile(StatementScope scope) {
		StatementScope blockScope = new BlockScope(scope);
		List<Statement> compiled = new ArrayList<>();
		for (ParsedStatement statement : statements) {
			compiled.add(statement.compile(blockScope));
		}
		return new BlockStatement(position, compiled);
	}
}
