/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.ConcatMap;

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

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		return this;
	}
}
