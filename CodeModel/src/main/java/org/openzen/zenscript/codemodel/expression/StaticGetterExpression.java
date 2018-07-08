/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.member.ref.GetterMemberRef;

/**
 *
 * @author Hoofdgebruiker
 */
public class StaticGetterExpression extends Expression {
	public final GetterMemberRef getter;
	
	public StaticGetterExpression(CodePosition position, GetterMemberRef getter) {
		super(position, getter.type, null);
		
		this.getter = getter;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitStaticGetter(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}
}
