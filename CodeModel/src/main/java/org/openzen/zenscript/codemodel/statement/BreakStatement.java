/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class BreakStatement extends Statement {
	public final Statement target;
	
	public BreakStatement(CodePosition position, Statement target) {
		super(position);
		
		this.target = target;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitBreak(this);
	}
}
