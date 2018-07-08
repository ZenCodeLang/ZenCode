/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.Arrays;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class CallArguments {
	public static final ITypeID[] NO_TYPE_ARGUMENTS = new ITypeID[0];
	public static final CallArguments EMPTY = new CallArguments(new Expression[0]);
	
	public final ITypeID[] typeArguments;
	public final Expression[] arguments;
	
	public CallArguments(Expression... arguments) {
		this.typeArguments = NO_TYPE_ARGUMENTS;
		this.arguments = arguments;
	}
	
	public CallArguments(ITypeID[] typeArguments, Expression[] arguments) {
		if (typeArguments == null)
			typeArguments = NO_TYPE_ARGUMENTS;
		if (arguments == null)
			throw new IllegalArgumentException("Arguments cannot be null!");
		
		this.typeArguments = typeArguments;
		this.arguments = arguments;
	}
	
	public int getNumberOfTypeArguments() {
		return typeArguments.length;
	}
	
	public CallArguments transform(ExpressionTransformer transformer) {
		Expression[] tArguments = Expression.transform(arguments, transformer);
		return tArguments == arguments ? this : new CallArguments(typeArguments, tArguments);
	}
	
	public CallArguments normalize(CodePosition position, TypeScope scope, FunctionHeader header) {
		CallArguments result = this;
		
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = arguments[i].castImplicit(position, scope, header.parameters[i].type);
		}
		
		if (arguments.length < header.parameters.length) {
			Expression[] newArguments = Arrays.copyOf(arguments, header.parameters.length);
			for (int i = arguments.length; i < header.parameters.length; i++) {
				if (header.parameters[i].defaultValue == null)
					throw new CompileException(position, CompileExceptionCode.MISSING_PARAMETER, "Parameter missing and no default value specified");
				newArguments[i] = header.parameters[i].defaultValue;
			}
			result = new CallArguments(typeArguments, newArguments);
		}
		return result;
	}
}
