package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;

import java.util.function.Consumer;

public class BreakStatement extends Statement {
	public final LoopStatement target;

	public BreakStatement(CodePosition position, LoopStatement target) {
		super(position, null);

		this.target = target;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitBreak(this);
	}

	@Override
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitBreak(context, this);
	}

	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		return modified.contains(target) ? new BreakStatement(position, modified.getAt(target)) : this;
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		return modified.contains(target) ? new BreakStatement(position, modified.getAt(target)) : this;
	}
}
