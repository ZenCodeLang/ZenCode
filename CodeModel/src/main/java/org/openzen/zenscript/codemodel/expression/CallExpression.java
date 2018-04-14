/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class CallExpression extends Expression {
	public final Expression target;
	public final FunctionalMember member;
	public final CallArguments arguments;
	
	public CallExpression(CodePosition position, Expression target, FunctionalMember member, FunctionHeader instancedHeader, CallArguments arguments) {
		super(position, instancedHeader.returnType);
		
		this.target = target;
		this.member = member;
		this.arguments = arguments;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCall(this);
	}
}
