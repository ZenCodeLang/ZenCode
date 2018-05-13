/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 * Using to cast a class type to a base type.
 * 
 * @author Hoofdgebruiker
 */
public class SupertypeCastExpression extends Expression {
	public final Expression value;
	
	public SupertypeCastExpression(CodePosition position, Expression value, ITypeID type) {
		super(position, type);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSupertypeCast(this);
	}
}
