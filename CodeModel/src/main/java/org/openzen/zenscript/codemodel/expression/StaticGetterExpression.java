/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class StaticGetterExpression extends Expression {
	public final GetterMember getter;
	
	public StaticGetterExpression(CodePosition position, GetterMember getter) {
		super(position, getter.type, getter.header.thrownType);
		
		this.getter = getter;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitStaticGetter(this);
	}
}
