/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.AndAndExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionAndAnd extends ParsedExpression {
	private final ParsedExpression left;
	private final ParsedExpression right;

	public ParsedExpressionAndAnd(
			CodePosition position,
			ParsedExpression left,
			ParsedExpression right) {
		super(position);

		this.left = left;
		this.right = right;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		Expression left = this.left.compile(scope).eval();
		Expression right = this.right.compile(scope).eval();

		TypeID resultType = scope.getTypeMembers(left.type).union(right.type);
		if (resultType == null)
			throw new CompileException(position, CompileExceptionCode.TYPE_CANNOT_UNITE, "These types could not be unified: " + left.type + " and " + right.type);
		
		left = left.castImplicit(position, scope, resultType);
		right = right.castImplicit(position, scope, resultType);
		
		return new AndAndExpression(position, left, right);
	}

	@Override
	public boolean hasStrongType() {
		return left.hasStrongType() && right.hasStrongType();
	}
}
