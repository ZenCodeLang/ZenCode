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
public class StaticSetterExpression extends Expression {
	public final SetterMemberRef setter;
	public final Expression value;
	
	public StaticSetterExpression(CodePosition position, SetterMemberRef setter, Expression value) {
		super(position, setter.type, value.thrownType);
		
		this.setter = setter;
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitStaticSetter(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		return tValue == value ? this : new StaticSetterExpression(position, setter, tValue);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new StaticSetterExpression(position, setter, value.normalize(scope));
	}
}
