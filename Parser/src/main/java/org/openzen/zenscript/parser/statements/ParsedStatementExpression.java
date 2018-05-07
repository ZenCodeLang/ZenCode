package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.shared.CodePosition;

public class ParsedStatementExpression extends ParsedStatement {
	private final ParsedExpression expression;

	public ParsedStatementExpression(CodePosition position, WhitespaceInfo whitespace, ParsedExpression expression) {
		super(position, whitespace);

		this.expression = expression;
	}

	@Override
	public Statement compile(StatementScope scope) {
		return result(new ExpressionStatement(position, this.expression.compile(new ExpressionScope(scope)).eval()));
	}
}
