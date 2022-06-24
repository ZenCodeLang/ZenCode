package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.scope.TypeScope;

import java.util.function.Consumer;

public class ExpressionStatement extends Statement {
	public final Expression expression;

	public ExpressionStatement(CodePosition position, Expression expression) {
		super(position, expression.thrownType);

		this.expression = expression;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitExpression(this);
	}

	@Override
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitExpression(context, this);
	}

	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
		expression.forEachStatement(consumer);
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tExpression = expression.transform(transformer);
		return tExpression == expression ? this : new ExpressionStatement(position, tExpression);
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tExpression = expression.transform(transformer);
		return tExpression == expression ? this : new ExpressionStatement(position, tExpression);
	}
}
