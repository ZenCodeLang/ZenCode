/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class NewExpression extends Expression {
	public final ConstructorMember constructor;
	public final CallArguments arguments;
	
	public NewExpression(CodePosition position, ITypeID type, ConstructorMember constructor, CallArguments arguments) {
		super(position, type);
		
		this.constructor = constructor;
		this.arguments = arguments;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitNew(this);
	}
}
