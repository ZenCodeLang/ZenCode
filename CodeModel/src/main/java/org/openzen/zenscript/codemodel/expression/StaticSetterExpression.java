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
public class StaticSetterExpression extends Expression {
	public final SetterMember setter;
	public final Expression value;
	
	public StaticSetterExpression(CodePosition position, SetterMember setter, Expression value) {
		super(position, setter.type, binaryThrow(position, setter.header.thrownType, value.thrownType));
		
		this.setter = setter;
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitStaticSetter(this);
	}
}
