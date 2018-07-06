/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class CallStaticExpression extends Expression {
	public final FunctionalMemberRef member;
	public final ITypeID target;
	public final CallArguments arguments;
	public final FunctionHeader instancedHeader;
	
	public CallStaticExpression(CodePosition position, ITypeID target, FunctionalMemberRef member, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		super(position, instancedHeader.returnType, multiThrow(position, arguments.arguments));
		
		this.member = member;
		this.target = target;
		this.arguments = scope == null ? arguments : arguments.normalize(position, scope, instancedHeader);
		this.instancedHeader = instancedHeader;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCallStatic(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		CallArguments tArguments = arguments.transform(transformer);
		return arguments == tArguments ? this : new CallStaticExpression(position, target, member, instancedHeader, tArguments, null);
	}
}
