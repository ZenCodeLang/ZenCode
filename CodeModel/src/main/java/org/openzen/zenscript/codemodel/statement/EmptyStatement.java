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
public class EmptyStatement extends Statement {
	public EmptyStatement(CodePosition position) {
		super(position, null);
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitEmpty(this);
	}
}
