package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

public class ParsedStatementReturn extends ParsedStatement {
	private final ParsedExpression expression;

	public ParsedStatementReturn(CodePosition position, ParsedExpression expression) {
		super(position);
		
		this.expression = expression;
	}

	public ParsedExpression getExpression() {
		return expression;
	}

	@Override
	public Statement compile(StatementScope scope) {
		if (expression == null) {
			if (scope.getFunctionHeader().returnType == BasicTypeID.VOID)
				return new ReturnStatement(position, null);
			else
				throw new CompileException(position, CompileExceptionCode.RETURN_VALUE_REQUIRED, "Return value is required");
		} else if (scope.getFunctionHeader().returnType == BasicTypeID.VOID) {
			throw new CompileException(position, CompileExceptionCode.RETURN_VALUE_VOID, "Cannot return a value from a void function");
		} else {
			Expression value = expression
					.compile(new ExpressionScope(scope, scope.getFunctionHeader().returnType))
					.eval()
					.castImplicit(position, scope, scope.getFunctionHeader().returnType);
			return new ReturnStatement(position, value);
		}
	}
}
