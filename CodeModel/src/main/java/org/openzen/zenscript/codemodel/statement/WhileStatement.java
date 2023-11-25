package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.function.Consumer;

public class WhileStatement extends LoopStatement {
	public final Expression condition;
	public Statement content;

	public WhileStatement(CodePosition position, String label, Expression condition, ObjectId objectId) {
		super(position, label, null, objectId);
		this.condition = condition;
	}

	public void setContent(Statement content) {
		setThrownType(content.getThrownType());
		this.content = content;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitWhile(this);
	}

	@Override
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitWhile(context, this);
	}

	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
		content.forEachStatement(consumer);
	}

	@Override
	public WhileStatement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tCondition = condition.transform(transformer);
		WhileStatement result = new WhileStatement(position, label, tCondition, objectId);
		result.content = content.transform(transformer, modified.concat(this, result));
		return result;
	}

	@Override
	public WhileStatement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tCondition = condition.transform(transformer);
		WhileStatement result = new WhileStatement(position, label, tCondition, objectId);
		result.content = content.transform(transformer, modified.concat(this, result));
		return result;
	}

	@Override
	public TypeID getReturnType() {
		return content.getReturnType();
	}
}
