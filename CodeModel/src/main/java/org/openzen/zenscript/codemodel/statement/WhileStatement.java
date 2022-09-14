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

	public WhileStatement(CodePosition position, String label, Expression condition) {
		super(position, label, null); // TODO: thrown type

		this.condition = condition;
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
		Statement tContent = content.transform(transformer, modified);
		if (condition == tCondition && content == tContent)
			return this;

		WhileStatement result = new WhileStatement(position, label, tCondition);
		result.content = content.transform(transformer, modified.concat(this, result));
		return result;
	}

	@Override
	public WhileStatement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tCondition = condition.transform(transformer);
		Statement tContent = content.transform(transformer, modified);
		if (condition == tCondition && content == tContent)
			return this;

		WhileStatement result = new WhileStatement(position, label, tCondition);
		result.content = content.transform(transformer, modified.concat(this, result));
		return result;
	}

	@Override
	public TypeID getReturnType() {
		return content.getReturnType();
	}

	@Override
	public VarStatement[] getLoopVariables() {
		return VarStatement.NONE;
	}
}
