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
public class DoWhileStatement extends LoopStatement {
	public Statement content;
	public final Expression condition;
	
	public DoWhileStatement(CodePosition position, String label, Expression condition) {
		super(position, label);
		
		this.condition = condition;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitDoWhile(this);
	}
}
