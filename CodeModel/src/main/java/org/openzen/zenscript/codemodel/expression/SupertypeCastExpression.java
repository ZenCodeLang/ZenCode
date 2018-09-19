/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 * Using to cast a class type to a base type.
 * 
 * @author Hoofdgebruiker
 */
public class SupertypeCastExpression extends Expression {
	public final Expression value;
	
	public SupertypeCastExpression(CodePosition position, Expression value, ITypeID type) {
		super(position, type, value.thrownType);
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSupertypeCast(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitSupertypeCast(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new SupertypeCastExpression(position, tValue, type);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new SupertypeCastExpression(position, value.normalize(scope), type.getNormalized());
	}
}
