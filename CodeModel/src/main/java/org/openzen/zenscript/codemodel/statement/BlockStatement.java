/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import java.util.List;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class BlockStatement extends Statement {
	public final List<Statement> statements;
	
	public BlockStatement(CodePosition position, List<Statement> statements) {
		super(position);
		
		this.statements = statements;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitBlock(this);
	}
}
