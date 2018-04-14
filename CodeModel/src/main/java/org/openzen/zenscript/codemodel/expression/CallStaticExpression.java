/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class CallStaticExpression extends Expression {
	public final FunctionalMember member;
	public final CallArguments arguments;
	
	public CallStaticExpression(CodePosition position, FunctionalMember member, CallArguments arguments) {
		super(position, member.header.returnType);
		
		this.member = member;
		this.arguments = arguments;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCallStatic(this);
	}
}
