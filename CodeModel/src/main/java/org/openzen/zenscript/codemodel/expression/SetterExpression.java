/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.member.ref.SetterMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class SetterExpression extends Expression {
	public final Expression target;
	public final SetterMemberRef setter;
	public final Expression value;
	
	public SetterExpression(CodePosition position, Expression target, SetterMemberRef setter, Expression value) {
		super(position, setter.getType(), value.thrownType);
		
		this.target = target;
		this.setter = setter;
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSetter(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tTarget = target.transform(transformer);
		Expression tValue = value.transform(transformer);
		return tTarget == target && tValue == value ? this : new SetterExpression(position, tTarget, setter, tValue);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new SetterExpression(position, target.normalize(scope), setter, value.normalize(scope));
	}
}
