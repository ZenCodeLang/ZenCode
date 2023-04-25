package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

/**
 * Compare expression for basic types. Left and right MUST be of the same type,
 * and MUST be a basic type. (any integer or floating point type, char, string)
 */
public class CompareExpression extends Expression {
	public final Expression left;
	public final Expression right;
	public final MethodInstance operator;
	public final CompareType comparison;

	public CompareExpression(CodePosition position, Expression left, Expression right, MethodInstance operator, CompareType comparison) {
		super(position, BasicTypeID.BOOL, binaryThrow(position, left.thrownType, right.thrownType));

		this.left = left;
		this.right = right;
		this.operator = operator;
		this.comparison = comparison;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCompare(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitCompare(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tLeft = left.transform(transformer);
		Expression tRight = right.transform(transformer);
		return left == tLeft && right == tRight ? this : new CompareExpression(position, tLeft, tRight, operator, comparison);
	}
}
