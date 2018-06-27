/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.ConcatMap;

/**
 *
 * @author Hoofdgebruiker
 */
public class DoWhileStatement extends LoopStatement {
	public Statement content;
	public final Expression condition;
	
	public DoWhileStatement(CodePosition position, String label, Expression condition) {
		super(position, label, null); // TODO: thrown type
		
		this.condition = condition;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitDoWhile(this);
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tCondition = condition.transform(transformer);
		DoWhileStatement result = new DoWhileStatement(position, label, tCondition);
		result.content = content.transform(transformer, modified.concat(this, result));
		return result;
	}
}
