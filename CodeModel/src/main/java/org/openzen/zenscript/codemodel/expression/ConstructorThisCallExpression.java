/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstructorThisCallExpression extends Expression {
	public final ITypeID objectType;
	public final ConstructorMember constructor;
	public final CallArguments arguments;
	
	public ConstructorThisCallExpression(CodePosition position, ITypeID type, ConstructorMember constructor, CallArguments arguments) {
		super(position, BasicTypeID.VOID);
		
		this.objectType = type;
		this.constructor = constructor;
		this.arguments = arguments;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConstructorThisCall(this);
	}
}
