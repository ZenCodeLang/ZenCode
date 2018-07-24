/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstructorSuperCallExpression extends Expression {
	public final ITypeID objectType;
	public final FunctionalMemberRef constructor;
	public final CallArguments arguments;
	
	public ConstructorSuperCallExpression(CodePosition position, ITypeID type, FunctionalMemberRef constructor, CallArguments arguments) {
		super(position, BasicTypeID.VOID, binaryThrow(position, constructor.header.thrownType, multiThrow(position, arguments.arguments)));
		
		this.objectType = type;
		this.constructor = constructor;
		this.arguments = arguments;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitConstructorSuperCall(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		CallArguments tArguments = arguments.transform(transformer);
		return tArguments == arguments ? this : new ConstructorSuperCallExpression(position, type, constructor, tArguments);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new ConstructorSuperCallExpression(position, type.getNormalized(), constructor, arguments.normalize(position, scope, constructor.header));
	}
}
