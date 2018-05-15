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
public class LockStatement extends Statement {
	public final Expression object;
	public final Statement content;
	
	public LockStatement(CodePosition position, Expression object, Statement content) {
		super(position, Expression.binaryThrow(position, object.thrownType, content.thrownType));
		
		this.object = object;
		this.content = content;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitLock(this);
	}
}
