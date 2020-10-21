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
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;

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
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitDoWhile(context, this);
	}
	
	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
		content.forEachStatement(consumer);
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tCondition = condition.transform(transformer);
		Statement tContent = content.transform(transformer, modified);
		if (tCondition == condition && tContent == content)
			return this;
		
		DoWhileStatement result = new DoWhileStatement(position, label, condition);
		result.content = content.transform(transformer, modified.concat(this, result));
		return result;
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tCondition = condition.transform(transformer);
		Statement tContent = content.transform(transformer, modified);
		if (tCondition == condition && tContent == content)
			return this;
		
		DoWhileStatement result = new DoWhileStatement(position, label, tCondition);
		result.content = content.transform(transformer, modified.concat(this, result));
		return result;
	}

	@Override
	public Statement normalize(TypeScope scope, ConcatMap<LoopStatement, LoopStatement> modified) {
		DoWhileStatement result = new DoWhileStatement(position, label, condition.normalize(scope));
		result.content = content.normalize(scope, modified.concat(this, result));
		return result;
	}

	@Override
	public StoredType getReturnType() {
		return content.getReturnType();
	}
}
