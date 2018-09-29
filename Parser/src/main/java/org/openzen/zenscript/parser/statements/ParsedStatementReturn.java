package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.expression.ParsedExpression;

public class ParsedStatementReturn extends ParsedStatement {
	private final ParsedExpression expression;

	public ParsedStatementReturn(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, ParsedExpression expression) {
		super(position, annotations, whitespace);
		
		this.expression = expression;
	}

	public ParsedExpression getExpression() {
		return expression;
	}

	@Override
	public Statement compile(StatementScope scope) {
		if (expression == null) {
			return new ReturnStatement(position, null);
		} else {
			Expression value = expression
					.compile(new ExpressionScope(scope, scope.getFunctionHeader().getReturnType()))
					.eval()
					.castImplicit(position, scope, scope.getFunctionHeader().getReturnType());
			return result(new ReturnStatement(position, value), scope);
		}
	}
}
