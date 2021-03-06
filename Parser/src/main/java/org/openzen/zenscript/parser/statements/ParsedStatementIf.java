package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.statement.IfStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.expression.ParsedExpression;

public class ParsedStatementIf extends ParsedStatement {
	private final ParsedExpression condition;
	private final ParsedStatement onThen;
	private final ParsedStatement onElse;

	public ParsedStatementIf(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, ParsedExpression condition, ParsedStatement onThen, ParsedStatement onElse) {
		super(position, annotations, whitespace);

		this.condition = condition;
		this.onThen = onThen;
		this.onElse = onElse;
	}

	@Override
	public Statement compile(StatementScope scope) {
		Expression condition;
		try {
			condition = this.condition.compile(new ExpressionScope(scope, BasicTypeID.HINT_BOOL)).eval();
		} catch (CompileException ex) {
			condition = new InvalidExpression(BasicTypeID.BOOL, ex);
		}

		Statement onThen = this.onThen.compile(scope);
		Statement onElse = this.onElse == null ? null : this.onElse.compile(scope);
		return result(new IfStatement(position, condition, onThen, onElse), scope);
	}
}
