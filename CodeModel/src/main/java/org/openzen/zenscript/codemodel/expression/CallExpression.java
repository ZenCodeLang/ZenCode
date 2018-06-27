/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class CallExpression extends Expression {
	public final Expression target;
	public final FunctionalMember member;
	public final CallArguments arguments;
	public final FunctionHeader instancedHeader;
	
	public CallExpression(CodePosition position, Expression target, FunctionalMember member, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		super(position, instancedHeader.returnType, multiThrow(position, arguments.arguments));
		
		this.target = target;
		this.member = member;
		this.arguments = scope == null ? arguments : arguments.normalize(position, scope, instancedHeader);
		this.instancedHeader = instancedHeader;
	}
	
	public Expression getFirstArgument() {
		return arguments.arguments[0];
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCall(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tTarget = target.transform(transformer);
		CallArguments tArguments = arguments.transform(transformer);
		return tTarget == target && tArguments == arguments
				? this
				: new CallExpression(position, tTarget, member, instancedHeader, tArguments, null);
	}
}
