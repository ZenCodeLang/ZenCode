/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.ConcatMap;

/**
 *
 * @author Hoofdgebruiker
 */
public class IfStatement extends Statement {
	public final Expression condition;
	public final Statement onThen;
	public final Statement onElse;
	
	public IfStatement(CodePosition position, Expression condition, Statement onThen, Statement onElse) {
		super(position, getThrownType(condition, onThen, onElse));
		
		this.condition = condition;
		this.onThen = onThen;
		this.onElse = onElse;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitIf(this);
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tCondition = condition.transform(transformer);
		Statement tOnThen = onThen.transform(transformer, modified);
		Statement tOnElse = onElse.transform(transformer, modified);
		return tCondition == condition && onThen == tOnThen && onElse == tOnElse
				? this
				: new IfStatement(position, tCondition, tOnThen, tOnElse);
	}
	
	private static ITypeID getThrownType(Expression condition, Statement onThen, Statement onElse) {
		ITypeID result = Expression.binaryThrow(onThen.position, condition.thrownType, onThen.thrownType);
		if (onElse != null)
			result = Expression.binaryThrow(onElse.position, result, onElse.thrownType);
		return result;
	}
}
