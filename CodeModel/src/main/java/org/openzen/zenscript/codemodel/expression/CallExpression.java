/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class CallExpression extends Expression {
	public final Expression target;
	public final FunctionalMemberRef member;
	public final CallArguments arguments;
	public final FunctionHeader instancedHeader;
	
	public CallExpression(CodePosition position, Expression target, FunctionalMemberRef member, FunctionHeader instancedHeader, CallArguments arguments) {
		super(position, instancedHeader.getReturnType(), multiThrow(position, arguments.arguments));
		
		this.target = target;
		this.member = member;
		this.arguments = arguments;
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
				: new CallExpression(position, tTarget, member, instancedHeader, tArguments);
	}
	
	@Override
	public String evaluateStringConstant() {
		if (member.getBuiltin() == null)
			throw new UnsupportedOperationException("Cannot evaluate to a string constant!");
		
		switch (member.getBuiltin()) {
			case STRING_ADD_STRING:
				return target.evaluateStringConstant() + arguments.arguments[0].evaluateStringConstant();
			default:
				throw new UnsupportedOperationException("Cannot evaluate to a string constant!");
		}
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new CallExpression(
				position,
				target.normalize(scope),
				member,
				instancedHeader.normalize(scope.getTypeRegistry()),
				arguments.normalize(position, scope, instancedHeader));
	}
}
