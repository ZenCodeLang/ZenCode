package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;
import java.util.function.Consumer;

public class ForeachStatement extends LoopStatement {
	public final VarStatement[] loopVariables;
	public final Expression list;
	public final IteratorInstance iterator;
	protected Statement content;

	public ForeachStatement(CodePosition position, VarStatement[] loopVariables, IteratorInstance iterator, Expression list, ObjectId objectId) {
		super(position, loopVariables[0].name, null, objectId);

		this.loopVariables = loopVariables;
		this.list = list;
		this.iterator = iterator;
	}

	public Statement getContent() {
		return content;
	}

	public void setContent(Statement content) {
		this.content = content;
		setThrownType(content.getThrownType());
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitForeach(this);
	}

	@Override
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitForeach(context, this);
	}

	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
		content.forEachStatement(consumer);
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tList = list.transform(transformer);
		ForeachStatement result = new ForeachStatement(position, loopVariables, iterator, tList, objectId);
		result.setContent(content.transform(transformer, modified.concat(this, result)));
		return result;
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tList = list.transform(transformer);
		ForeachStatement result = new ForeachStatement(position, loopVariables, iterator, tList, objectId);
		result.setContent(content.transform(transformer, modified.concat(this, result)));
		return result;
	}

	@Override
	public Optional<TypeID> getReturnType() {
		return content.getReturnType();
	}
}
