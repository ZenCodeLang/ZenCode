/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.member.DefinitionMemberGroup;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionBinary extends ParsedExpression {
	private final ParsedExpression left;
	private final ParsedExpression right;
	private final OperatorType operator;

	public ParsedExpressionBinary(CodePosition position, ParsedExpression left, ParsedExpression right, OperatorType operator) {
		super(position);

		this.left = left;
		this.right = right;
		this.operator = operator;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		Expression cLeft = left.compile(scope).eval();
		DefinitionMemberGroup members = scope.getTypeMembers(cLeft.type).getOrCreateGroup(this.operator);
		ExpressionScope innerScope = scope.withHints(members.predictCallTypes(scope, scope.getResultTypeHints(), 1)[0]);
		
		Expression cRight = right.compile(innerScope).eval();
		CallArguments arguments = new CallArguments(cRight);
		return members.call(position, scope, cLeft, arguments, false);
	}

	@Override
	public boolean hasStrongType() {
		return left.hasStrongType() && right.hasStrongType();
	}
}
