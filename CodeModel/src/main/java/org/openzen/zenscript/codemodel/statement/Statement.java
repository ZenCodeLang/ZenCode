package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.annotations.StatementAnnotation;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class Statement extends Taggable {
	public final CodePosition position;
	private TypeID thrownType;
	public StatementAnnotation[] annotations = StatementAnnotation.NONE;

	public Statement(CodePosition position, TypeID thrownType) {
		this.position = position;
		this.thrownType = thrownType;
	}

	public Optional<TypeID> getReturnType() {
		return Optional.empty();
	}

	public TypeID getThrownType() {
		return thrownType;
	}

	protected void setThrownType(TypeID thrownType) {
		this.thrownType = thrownType;
	}

	public abstract <T> T accept(StatementVisitor<T> visitor);

	public abstract <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor);

	public abstract void forEachStatement(Consumer<Statement> consumer);

	public final Statement transform(StatementTransformer transformer) {
		return transform(transformer, ConcatMap.empty(LoopStatement.class, LoopStatement.class));
	}

	public abstract Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified);

	public abstract Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified);
}
