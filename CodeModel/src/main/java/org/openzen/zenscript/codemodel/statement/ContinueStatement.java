package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.scope.TypeScope;

import java.util.function.Consumer;

public class ContinueStatement extends Statement {
	public final LoopStatement target;

	public ContinueStatement(CodePosition position, LoopStatement target) {
		super(position, null);

		this.target = target;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitContinue(this);
	}

	@Override
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitContinue(context, this);
	}

	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		return modified.contains(target) ? new ContinueStatement(position, modified.getAt(target)) : this;
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		return modified.contains(target) ? new ContinueStatement(position, modified.getAt(target)) : this;
	}

	@Override
	public Statement normalize(TypeScope scope, ConcatMap<LoopStatement, LoopStatement> modified) {
		return modified.contains(target) ? new ContinueStatement(position, modified.getAt(target)) : this;
	}
}
