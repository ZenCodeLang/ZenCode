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
public class ThrowStatement extends Statement {
	public final Expression value;
	
	public ThrowStatement(CodePosition position, Expression value) {
		super(position, value.type);
		
		this.value = value;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitThrow(this);
	}
}
