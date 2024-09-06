package org.openzen.zenscript.codemodel.compilation.impl;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.WrappedCompilingExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ComparisonOperator implements ResolvedType.Comparator {
	private final InstanceCallable operator;

	public ComparisonOperator(InstanceCallable operator) {
		this.operator = operator;
	}

	@Override
	public CastedExpression compare(ExpressionCompiler compiler, CodePosition position, Expression left, CompilingExpression right, CompareType type) {
		Expression leftValue = operator.call(compiler, position, left, TypeID.NONE, right);
		CompilingExpression rightValue = new WrappedCompilingExpression(compiler, compiler.at(position).constant(0));
		Expression result = compiler.at(position).compare(leftValue, rightValue, type);
		return CastedExpression.exact(result);
	}
}
