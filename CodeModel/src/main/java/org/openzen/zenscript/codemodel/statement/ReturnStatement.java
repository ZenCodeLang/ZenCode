package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;
import java.util.function.Consumer;

public class ReturnStatement extends Statement {
	public final Expression value;

	public ReturnStatement(CodePosition position, Expression value) {
		super(position, value == null ? null : value.thrownType);

		this.value = value;
	}

	@Override
	public Optional<TypeID> getReturnType() {
		return Optional.of(value != null ? value.type : BasicTypeID.VOID);
	}

	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitReturn(this);
	}

	@Override
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitReturn(context, this);
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tValue = value == null ? null : value.transform(transformer);
		return tValue == value ? this : new ReturnStatement(position, tValue);
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tValue = value == null ? null : value.transform(transformer);
		return tValue == value ? this : new ReturnStatement(position, tValue);
	}
}
