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
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.PrecompilationState;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionUnary extends ParsedExpression {
	private final ParsedExpression value;
	private final OperatorType operator;

	public ParsedExpressionUnary(CodePosition position, ParsedExpression value, OperatorType operator) {
		super(position);

		this.value = value;
		this.operator = operator;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		Expression cValue = value.compile(scope).eval();
		return scope.getTypeMembers(cValue.type).getOrCreateGroup(operator).call(position, scope, cValue, new CallArguments(Expression.NONE), false);
	}

	@Override
	public boolean hasStrongType() {
		return value.hasStrongType();
	}

	@Override
	public ITypeID precompileForType(ExpressionScope scope, PrecompilationState state) {
		ITypeID valueType = value.precompileForType(scope, state);
		if (valueType == null)
			return null;
		
		FunctionalMemberRef member = scope.getTypeMembers(valueType).getOrCreateGroup(operator).getUnaryMethod();
		if (member == null)
			return null;
		
		if (member.header.returnType == BasicTypeID.UNDETERMINED) {
			state.precompile(member.getTarget());
			member = scope.getTypeMembers(valueType).getOrCreateGroup(operator).getUnaryMethod();
		}
		return member.header.returnType;
	}
}
