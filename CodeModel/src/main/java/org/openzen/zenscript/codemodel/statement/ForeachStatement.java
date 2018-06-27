/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.member.IIteratorMember;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.ConcatMap;

/**
 *
 * @author Hoofdgebruiker
 */
public class ForeachStatement extends LoopStatement {
	public final VarStatement[] loopVariables;
	public final Expression list;
	public final IIteratorMember iterator;
	public Statement content;
	
	public ForeachStatement(CodePosition position, VarStatement[] loopVariables, IIteratorMember iterator, Expression list) {
		super(position, loopVariables[0].name, null); // TODO: thrown type
		
		this.loopVariables = loopVariables;
		this.list = list;
		this.iterator = iterator;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitForeach(this);
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tList = list.transform(transformer);
		ForeachStatement result = new ForeachStatement(position, loopVariables, iterator, tList);
		result.content = content.transform(transformer, modified.concat(this, result));
		return result;
	}
}
