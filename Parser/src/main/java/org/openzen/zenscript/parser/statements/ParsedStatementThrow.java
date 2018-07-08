/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.ThrowStatement;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.expression.ParsedExpression;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStatementThrow extends ParsedStatement {
	private final ParsedExpression expression;
	
	public ParsedStatementThrow(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, ParsedExpression expression) {
		super(position, annotations, whitespace);
		
		this.expression = expression;
	}

	@Override
	public Statement compile(StatementScope scope) {
		return result(new ThrowStatement(position, expression.compile(new ExpressionScope(scope)).eval()), scope);
	}
}
