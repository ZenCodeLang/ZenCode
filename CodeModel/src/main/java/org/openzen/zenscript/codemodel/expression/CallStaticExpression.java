/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class CallStaticExpression extends Expression {
	public final FunctionalMember member;
	public final ITypeID target;
	public final CallArguments arguments;
	public final FunctionHeader instancedHeader;
	
	public CallStaticExpression(CodePosition position, ITypeID target, FunctionalMember member, CallArguments arguments, FunctionHeader instancedHeader, TypeScope scope) {
		super(position, instancedHeader.returnType, multiThrow(position, arguments.arguments));
		
		this.member = member;
		this.target = target;
		this.arguments = arguments.normalize(position, scope, instancedHeader);
		this.instancedHeader = instancedHeader;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCallStatic(this);
	}
}
