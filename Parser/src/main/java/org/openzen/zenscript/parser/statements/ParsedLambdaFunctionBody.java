/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.parser.expression.ParsedExpression;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedLambdaFunctionBody extends ParsedFunctionBody {
	private final ParsedExpression value;
	
	public ParsedLambdaFunctionBody(ParsedExpression value) {
		this.value = value;
	}
	
	@Override
	public List<Statement> compile(StatementScope scope, FunctionHeader header) {
		List<Statement> statements = new ArrayList<>();
		if (header.returnType == BasicTypeID.VOID) {
			Expression value = this.value.compile(new ExpressionScope(scope)).eval();
			statements.add(new ExpressionStatement(value.position, value));
			statements.add(new ReturnStatement(value.position, null));
		} else {
			Expression returnValue = value
					.compile(new ExpressionScope(scope, header.returnType))
					.eval()
					.castImplicit(value.position, scope, header.returnType);
			statements.add(new ReturnStatement(value.position, returnValue));
		}
		return statements;
	}
}
