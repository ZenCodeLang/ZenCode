package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
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
			return result(new ReturnStatement(position, value), scope);
		}
	}
}
