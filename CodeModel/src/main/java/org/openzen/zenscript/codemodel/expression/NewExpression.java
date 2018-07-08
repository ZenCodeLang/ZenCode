/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class NewExpression extends Expression {
	public final FunctionalMemberRef constructor;
	public final CallArguments arguments;
	public final FunctionHeader instancedHeader;
	
	public NewExpression(
			CodePosition position,
			ITypeID type,
			FunctionalMemberRef constructor,
			CallArguments arguments)
	{
		super(position, type, binaryThrow(position, constructor.header.thrownType, multiThrow(position, arguments.arguments)));
		
		this.constructor = constructor;
		this.arguments = arguments;
		this.instancedHeader = constructor.header;
	}
	
	public NewExpression(
			CodePosition position,
			ITypeID type,
			FunctionalMemberRef constructor,
			CallArguments arguments,
			FunctionHeader instancedHeader,
			TypeScope scope)
	{
		super(position, type, binaryThrow(position, constructor.header.thrownType, multiThrow(position, arguments.arguments)));
		
		this.constructor = constructor;
		this.arguments = scope == null ? arguments : arguments.normalize(position, scope, instancedHeader);
		this.instancedHeader = instancedHeader;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitNew(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		CallArguments tArguments = arguments.transform(transformer);
		return tArguments == arguments ? this : new NewExpression(position, type, constructor, tArguments, instancedHeader, null);
	}
}
