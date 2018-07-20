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
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.member.DefinitionMemberGroup;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.PrecompilationState;

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
		
		Expression cRight = right.compile(scope.withHints(members.predictCallTypes(scope, scope.getResultTypeHints(), 1)[0])).eval();
		return members.call(position, scope, cLeft, new CallArguments(cRight), false);
	}

	@Override
	public boolean hasStrongType() {
		return left.hasStrongType() && right.hasStrongType();
	}

	@Override
	public ITypeID precompileForType(ExpressionScope scope, PrecompilationState state) {
		ITypeID leftType = left.precompileForType(scope, state);
		if (leftType == null)
			return null;
		
		DefinitionMemberGroup members = scope.getTypeMembers(leftType).getOrCreateGroup(this.operator);
		ExpressionScope innerScope = scope.withHints(members.predictCallTypes(scope, scope.getResultTypeHints(), 1)[0]);
		ITypeID rightType = right.precompileForType(innerScope, state);
		FunctionalMemberRef method = members.selectMethod(position, scope, new CallArguments(rightType), true, false);
		if (!state.precompile(method.getTarget()))
			return null;
		
		return method.getTarget().header.returnType; // TODO: this will not work properly for methods with type parameters
	}
}
