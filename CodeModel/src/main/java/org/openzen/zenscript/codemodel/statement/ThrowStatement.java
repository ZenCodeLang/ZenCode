/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import java.util.function.Consumer;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.ConcatMap;

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
	
	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new ThrowStatement(position, value);
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new ThrowStatement(position, value);
	}
}
