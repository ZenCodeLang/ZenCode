package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.VariableID;

import java.util.Optional;
import java.util.function.Consumer;

public class MemoizedExpression extends Expression {

	public final Expression target;
	private boolean accessedOnlyOnce = true;
	private VariableID variableID;


	public MemoizedExpression(Expression target) {
		super(target.position, target.type, target.thrownType);
		this.target = target;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		if (accessedOnlyOnce) {
			return target.accept(visitor);
		} else {
			return visitor.visitMemoized(this);
		}
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		if (accessedOnlyOnce) {
			return target.accept(context, visitor);
		} else {
			return visitor.visitMemoized(context, this);
		}
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression transform = target.transform(transformer);
		if(transform == target) {
			return this;
		}

		return new MemoizedExpression(transform);
	}

	@Override
	public boolean aborts() {
		return target.aborts();
	}

	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		target.forEachStatement(consumer);
	}

	@Override
	public Optional<CompileTimeConstant> evaluate() {
		return target.evaluate();
	}

	public void markAccessedMoreThanOnce() {
		accessedOnlyOnce = false;
	}

	public void setVariableID(VariableID variableID) {
		this.variableID = variableID;
	}

	public boolean hasVariableID() {
		return variableID != null;
	}

	public VariableID getVariableID() {
		return variableID;
	}
}
