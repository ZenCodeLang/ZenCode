/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.DoWhileStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.linker.LoopScope;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStatementDoWhile extends ParsedStatement {
	public final String label;
	public final ParsedStatement content;
	public final ParsedExpression condition;
	
	public ParsedStatementDoWhile(CodePosition position, String label, ParsedStatement content, ParsedExpression condition) {
		super(position);
		
		this.label = label;
		this.content = content;
		this.condition = condition;
	}

	@Override
	public Statement compile(StatementScope scope) {
		Statement content = this.content.compile(scope);
		Expression condition = this.condition
				.compile(new ExpressionScope(scope, BasicTypeID.HINT_BOOL))
				.eval()
				.castImplicit(position, scope, BasicTypeID.BOOL);
		
		DoWhileStatement result = new DoWhileStatement(position, label, condition);
		LoopScope innerScope = new LoopScope(result, scope);
		result.content = this.content.compile(innerScope);
		return result;
	}
}
