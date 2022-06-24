package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.annotations.StatementAnnotation;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.function.Consumer;

public abstract class Statement extends Taggable {
	public final CodePosition position;
	public final TypeID thrownType;
	public StatementAnnotation[] annotations = StatementAnnotation.NONE;

	public Statement(CodePosition position, TypeID thrownType) {
		this.position = position;
		this.thrownType = thrownType;
	}

	public TypeID getReturnType() {
		return null;
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
