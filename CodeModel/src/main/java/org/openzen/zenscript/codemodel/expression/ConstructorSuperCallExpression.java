/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstructorSuperCallExpression extends Expression {
	public final ITypeID objectType;
	public final ConstructorMember constructor;
	public final CallArguments arguments;
	
	public ConstructorSuperCallExpression(CodePosition position, ITypeID type, ConstructorMember constructor, CallArguments arguments, TypeScope scope) {
		super(position, BasicTypeID.VOID, binaryThrow(position, constructor.header.thrownType, multiThrow(position, arguments.arguments)));
		
		this.objectType = type;
		this.constructor = constructor;
		this.arguments = scope == null ? arguments : arguments.normalize(position, scope, constructor.header);
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConstructorSuperCall(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		CallArguments tArguments = arguments.transform(transformer);
		return tArguments == arguments ? this : new ConstructorSuperCallExpression(position, type, constructor, tArguments, null);
	}
}
