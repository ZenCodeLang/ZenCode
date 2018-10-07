/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.member.ref.ImplementationMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class InterfaceCastExpression extends Expression {
	public final Expression value;
	public final ImplementationMemberRef implementation;
	
	public InterfaceCastExpression(CodePosition position, Expression value, ImplementationMemberRef implementation) {
		super(position, implementation.implementsType, value.thrownType);
		
		this.value = value;
		this.implementation = implementation;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitInterfaceCast(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitInterfaceCast(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		return value == tValue ? this : new InterfaceCastExpression(position, tValue, implementation);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new InterfaceCastExpression(position, value.normalize(scope), implementation);
	}
}
