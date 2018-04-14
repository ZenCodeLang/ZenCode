package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.IfStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.shared.CodePosition;

public class ParsedStatementIf extends ParsedStatement {
	private final ParsedExpression condition;
	private final ParsedStatement onThen;
	private final ParsedStatement onElse;

	public ParsedStatementIf(CodePosition position, ParsedExpression condition, ParsedStatement onThen, ParsedStatement onElse) {
		super(position);

		this.condition = condition;
		this.onThen = onThen;
		this.onElse = onElse;
	}

	@Override
	public Statement compile(StatementScope scope) {
		Expression condition = this.condition.compile(new ExpressionScope(scope, BasicTypeID.HINT_BOOL)).eval();
		Statement onThen = this.onThen.compile(scope);
		Statement onElse = this.onElse == null ? null : this.onElse.compile(scope);
		return new IfStatement(position, condition, onThen, onElse);
	}
}
