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
public class GetterExpression extends Expression {
	public final Expression target;
	public final GetterMember getter;
	
	public GetterExpression(CodePosition position, Expression target, GetterMember getter) {
		super(position, getter.type);
		
		this.target = target;
		this.getter = getter;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitGetter(this);
	}
}
