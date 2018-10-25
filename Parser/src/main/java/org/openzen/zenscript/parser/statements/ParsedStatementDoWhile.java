/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.statement.DoWhileStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.LoopScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.expression.ParsedExpression;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStatementDoWhile extends ParsedStatement {
	public final String label;
	public final ParsedStatement content;
	public final ParsedExpression condition;
	
	public ParsedStatementDoWhile(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String label, ParsedStatement content, ParsedExpression condition) {
		super(position, annotations, whitespace);
		
		this.label = label;
		this.content = content;
		this.condition = condition;
	}

	@Override
	public Statement compile(StatementScope scope) {
		Expression condition;
		try {
			condition = this.condition
				.compile(new ExpressionScope(scope, BasicTypeID.HINT_BOOL))
				.eval()
				.castImplicit(position, scope, BasicTypeID.BOOL.stored);
		} catch (CompileException ex) {
			condition = new InvalidExpression(BasicTypeID.BOOL.stored, ex);
		}
		
		DoWhileStatement result = new DoWhileStatement(position, label, condition);
		LoopScope innerScope = new LoopScope(result, scope);
		result.content = this.content.compile(innerScope);
		return result(result, scope);
	}
}
