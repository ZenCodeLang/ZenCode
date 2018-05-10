/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.member.FunctionalMember;
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
	
	public CallStaticExpression(CodePosition position, ITypeID target, FunctionalMember member, CallArguments arguments) {
		super(position, member.header.returnType);
		
		this.member = member;
		this.target = target;
		this.arguments = arguments;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCallStatic(this);
	}
}
