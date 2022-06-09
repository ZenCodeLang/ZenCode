package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.*;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.scope.TypeScope;

import java.util.function.Consumer;

public class InvalidStatement extends Statement {
	public final CompileError error;

	public InvalidStatement(CodePosition position, CompileError error) {
		super(position, null);
		this.error = error;
	}

	public InvalidStatement(CompileException ex) {
		super(ex.position, null);
		this.error = new CompileError(ex.code, ex.message);
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitInvalid(this);
	}

	@Override
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitInvalid(context, this);
	}

	@Override
	public void forEachStatement(Consumer<Statement> consumer) {

	}

	@Override
	public Statement normalize(TypeScope scope, ConcatMap<LoopStatement, LoopStatement> modified) {
		return this;
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		return this;
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		return this;
	}
}
