/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ExpressionStatement extends Statement {
	public final Expression expression;
	
	public ExpressionStatement(CodePosition position, Expression expression) {
		super(position, expression.thrownType);
		
		this.expression = expression;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitExpression(this);
	}
}
