/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class SetterExpression extends Expression {
	public final Expression target;
	public final SetterMember setter;
	public final Expression value;
	
	public SetterExpression(CodePosition position, Expression target, SetterMember setter, Expression value) {
		super(position, setter.type, binaryThrow(position, value.thrownType, setter.header.thrownType));
		
		this.target = target;
		this.setter = setter;
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSetter(this);
	}
}
