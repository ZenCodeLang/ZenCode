/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.shared.CodePosition;

/**
 * Used for post-increment and post-decrement.
 * 
 * @author Hoofdgebruiker
 */
public class PostCallExpression extends Expression {
	public final Expression target;
	public final OperatorMember member;
	
	public PostCallExpression(CodePosition position, Expression target, OperatorMember member, FunctionHeader instancedHeader) {
		super(position, instancedHeader.returnType);
		
		if (member.operator != OperatorType.DECREMENT && member.operator != OperatorType.INCREMENT)
			throw new IllegalArgumentException("Operator must be increment or decrement");
		
		this.target = target;
		this.member = member;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitPostCall(this);
	}
}
