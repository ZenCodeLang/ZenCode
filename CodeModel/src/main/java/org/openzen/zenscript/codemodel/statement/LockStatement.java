/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import java.util.function.Consumer;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;

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
	
	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
		content.forEachStatement(consumer);
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tObject = object.transform(transformer);
		Statement tContent = content.transform(transformer, modified);
		return tObject == object && tContent == content ? this : new LockStatement(position, tObject, tContent);
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tObject = object.transform(transformer);
		Statement tContent = content.transform(transformer, modified);
		return tObject == object && tContent == content ? this : new LockStatement(position, tObject, tContent);
	}
}
