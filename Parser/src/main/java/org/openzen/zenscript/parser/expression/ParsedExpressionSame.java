/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionSame extends ParsedExpression {
	private final ParsedExpression left;
	private final ParsedExpression right;
	private final boolean inverse;

	public ParsedExpressionSame(
			CodePosition position,
			ParsedExpression left,
			ParsedExpression right,
			boolean inverse) {
		super(position);

		this.left = left;
		this.right = right;
		this.inverse = inverse;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		Expression cLeft = left.compile(scope.withoutHints()).eval();
		Expression cRight = right.compile(scope.withHint(cLeft.type)).eval();
		return scope.getTypeMembers(cLeft.type)
				.getOrCreateGroup(inverse ? OperatorType.NOTSAME : OperatorType.SAME)
				.call(position, scope, cLeft, new CallArguments(cRight), false);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
