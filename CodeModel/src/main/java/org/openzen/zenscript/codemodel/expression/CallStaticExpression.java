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
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class CallStaticExpression extends Expression {
	public final FunctionalMemberRef member;
	public final ITypeID target;
	public final CallArguments arguments;
	public final FunctionHeader instancedHeader;
	
	public CallStaticExpression(CodePosition position, ITypeID target, FunctionalMemberRef member, FunctionHeader instancedHeader, CallArguments arguments) {
		super(position, instancedHeader.returnType, multiThrow(position, arguments.arguments));
		
		this.member = member;
		this.target = target;
		this.arguments = arguments;
		this.instancedHeader = instancedHeader;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCallStatic(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		CallArguments tArguments = arguments.transform(transformer);
		return arguments == tArguments ? this : new CallStaticExpression(position, target, member, instancedHeader, tArguments);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new CallStaticExpression(
				position,
				target.getNormalized(),
				member,
				instancedHeader.normalize(scope.getTypeRegistry()),
				arguments.normalize(position, scope, instancedHeader));
	}
}
