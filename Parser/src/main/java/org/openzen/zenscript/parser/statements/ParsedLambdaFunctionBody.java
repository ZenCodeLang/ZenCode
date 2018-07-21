/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.expression.ParsedExpression;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedLambdaFunctionBody implements ParsedFunctionBody {
	private final ParsedExpression value;
	
	public ParsedLambdaFunctionBody(ParsedExpression value) {
		this.value = value;
	}
	
	@Override
	public Statement compile(StatementScope scope, FunctionHeader header) {
		if (header.returnType == BasicTypeID.VOID) {
			Expression value = this.value.compile(new ExpressionScope(scope)).eval();
			return new ExpressionStatement(value.position, value);
		} else {
			Expression returnValue = value
					.compile(new ExpressionScope(scope, header.returnType))
					.eval()
					.castImplicit(value.position, scope, header.returnType);
			return new ReturnStatement(value.position, returnValue);
		}
	}

	@Override
	public ITypeID precompileForResultType(StatementScope scope, PrecompilationState precompileState) {
		return value.precompileForType(new ExpressionScope(scope), precompileState);
	}
}
