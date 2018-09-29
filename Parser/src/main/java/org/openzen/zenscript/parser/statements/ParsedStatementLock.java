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
import org.openzen.zenscript.codemodel.statement.LockStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.statement.InvalidStatement;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.expression.ParsedExpression;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStatementLock extends ParsedStatement {
	public final ParsedExpression object;
	public final ParsedStatement content;
	
	public ParsedStatementLock(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, ParsedExpression object, ParsedStatement content) {
		super(position, annotations, whitespace);
		
		this.object = object;
		this.content = content;
	}

	@Override
	public Statement compile(StatementScope scope) {
		try {
			Expression object = this.object.compile(new ExpressionScope(scope)).eval();
			Statement content = this.content.compile(scope);
			return result(new LockStatement(position, object, content), scope);
		} catch (CompileException ex) {
			return result(new InvalidStatement(ex), scope);
		}
	}
}
