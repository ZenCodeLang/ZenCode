/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class CallArguments {
	public static final CallArguments EMPTY = new CallArguments(new Expression[0]);
	public static final ITypeID[] NO_TYPE_ARGUMENTS = new ITypeID[0];
	
	public final ITypeID[] typeArguments;
	public final Expression[] arguments;
	
	public CallArguments(Expression... arguments) {
		this.typeArguments = NO_TYPE_ARGUMENTS;
		this.arguments = arguments;
	}
	
	public CallArguments(ITypeID[] typeArguments, Expression[] arguments) {
		this.typeArguments = typeArguments;
		this.arguments = arguments;
	}
}
