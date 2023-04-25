package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.function.Consumer;

public class DoWhileStatement extends LoopStatement {
	public final Expression condition;
	public Statement content;

	public DoWhileStatement(CodePosition position, String label, Expression condition) {
		super(position, label, null);

		this.condition = condition;
	}

	public void setContent(Statement content) {
		setThrownType(content.getThrownType());
		this.content = content;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitDoWhile(this);
	}

	@Override
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitDoWhile(context, this);
	}

	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
		content.forEachStatement(consumer);
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tCondition = condition.transform(transformer);
		DoWhileStatement result = new DoWhileStatement(position, label, tCondition);
		result.setContent(content.transform(transformer, modified.concat(this, result)));
		return result;
	}

	@Override
	public Statement transform(ExpressionTransformer transformer,ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tCondition = condition.transform(transformer);
		DoWhileStatement result = new DoWhileStatement(position, label, tCondition);
		result.setContent(content.transform(transformer, modified.concat(this, result)));
		return result;
	}

	@Override
	public TypeID getReturnType() {
		return content.getReturnType();
	}
}
