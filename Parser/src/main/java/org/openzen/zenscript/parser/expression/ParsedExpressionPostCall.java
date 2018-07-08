/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionPostCall extends ParsedExpression {
	private final ParsedExpression value;
	private final OperatorType operator;

	public ParsedExpressionPostCall(CodePosition position, ParsedExpression value, OperatorType operator) {
		super(position);

		this.value = value;
		this.operator = operator;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		Expression cValue = value.compile(scope).eval();
		return scope.getTypeMembers(cValue.type).getOrCreateGroup(operator).callPostfix(position, scope, cValue);
	}

	@Override
	public boolean hasStrongType() {
		return value.hasStrongType();
	}
}
