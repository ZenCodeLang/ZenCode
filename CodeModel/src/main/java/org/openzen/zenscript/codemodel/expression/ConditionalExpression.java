package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ConditionalExpression extends Expression {
	public final Expression condition;
	public final Expression ifThen;
	public final Expression ifElse;

	public ConditionalExpression(
			CodePosition position,
			Expression condition,
			Expression ifThen,
			Expression ifElse,
			TypeID type) {
		super(position, type, binaryThrow(position, condition.thrownType, binaryThrow(position, ifThen.thrownType, ifElse.thrownType)));

		if (!ifThen.type.equals(ifElse.type))
			throw new AssertionError();

		this.condition = condition;
		this.ifThen = ifThen;
		this.ifElse = ifElse;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConditional(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitConditional(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tCondition = transformer.transform(condition);
		Expression tIfThen = transformer.transform(ifThen);
		Expression tIfElse = transformer.transform(ifElse);
		return tCondition == condition && tIfThen == ifThen && tIfElse == ifElse
				? this
				: new ConditionalExpression(position, tCondition, tIfThen, tIfElse, type);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new ConditionalExpression(position, condition.normalize(scope), ifThen.normalize(scope), ifElse.normalize(scope), type.getNormalized());
	}
}
