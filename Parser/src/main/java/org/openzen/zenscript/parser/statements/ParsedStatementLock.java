/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.LockStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStatementLock extends ParsedStatement {
	public final ParsedExpression object;
	public final ParsedStatement content;
	
	public ParsedStatementLock(CodePosition position, ParsedExpression object, ParsedStatement content) {
		super(position);
		
		this.object = object;
		this.content = content;
	}

	@Override
	public Statement compile(StatementScope scope) {
		Expression object = this.object.compile(new ExpressionScope(scope)).eval();
		Statement content = this.content.compile(scope);
		return new LockStatement(position, object, content);
	}
}
