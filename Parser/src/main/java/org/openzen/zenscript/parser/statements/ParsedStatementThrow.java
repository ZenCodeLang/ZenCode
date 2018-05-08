/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.ThrowStatement;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStatementThrow extends ParsedStatement {
	private final ParsedExpression expression;
	
	public ParsedStatementThrow(CodePosition position, WhitespaceInfo whitespace, ParsedExpression expression) {
		super(position, whitespace);
		
		this.expression = expression;
	}

	@Override
	public Statement compile(StatementScope scope) {
		return result(new ThrowStatement(position, expression.compile(new ExpressionScope(scope)).eval()));
	}
}
