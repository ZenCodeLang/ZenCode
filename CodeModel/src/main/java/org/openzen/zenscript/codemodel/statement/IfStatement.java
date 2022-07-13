package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.function.Consumer;

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

	private static TypeID getThrownType(Expression condition, Statement onThen, Statement onElse) {
		TypeID result = Expression.binaryThrow(onThen.position, condition.thrownType, onThen.thrownType);
		if (onElse != null)
			result = Expression.binaryThrow(onElse.position, result, onElse.thrownType);
		return result;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitIf(this);
	}

	@Override
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitIf(context, this);
	}

	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
		onThen.forEachStatement(consumer);
		if (onElse != null)
			onElse.forEachStatement(consumer);
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tCondition = condition.transform(transformer);
		Statement tOnThen = onThen.transform(transformer, modified);
		Statement tOnElse = onElse == null ? null : onElse.transform(transformer, modified);
		return tCondition == condition && onThen == tOnThen && onElse == tOnElse
				? this
				: new IfStatement(position, tCondition, tOnThen, tOnElse);
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tCondition = condition.transform(transformer);
		Statement tOnThen = onThen.transform(transformer, modified);
		Statement tOnElse = onElse == null ? null : onElse.transform(transformer, modified);
		return tCondition == condition && onThen == tOnThen && onElse == tOnElse
				? this
				: new IfStatement(position, tCondition, tOnThen, tOnElse);
	}

	@Override
	public TypeID getReturnType() {
		final TypeID thenType = onThen.getReturnType();
		if (onElse == null)
			return thenType;
		final TypeID elseType = onElse.getReturnType();
		if (thenType == elseType)
			return thenType;

		if (thenType == null)
			return elseType;
		return thenType;
	}
}
