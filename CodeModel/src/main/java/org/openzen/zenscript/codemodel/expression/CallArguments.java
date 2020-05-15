/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.Arrays;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.*;

/**
 *
 * @author Hoofdgebruiker
 */
public class CallArguments {
	public static final CallArguments EMPTY = new CallArguments(new Expression[0]);
	
	public final StoredType[] typeArguments;
	public final Expression[] arguments;
	
	public CallArguments(Expression... arguments) {
		this.typeArguments = StoredType.NONE;
		this.arguments = arguments;
	}
	
	public CallArguments(StoredType[] typeArguments, Expression[] arguments) {
		if (typeArguments == null)
			typeArguments = StoredType.NONE;
		if (arguments == null)
			throw new IllegalArgumentException("Arguments cannot be null!");
		
		this.typeArguments = typeArguments;
		this.arguments = arguments;
	}
	
	public CallArguments(StoredType... dummy) {
		this.typeArguments = StoredType.NONE;
		this.arguments = new Expression[dummy.length];
		for (int i = 0; i < dummy.length; i++)
			arguments[i] = new DummyExpression(dummy[i]);
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
		
		boolean isVariadic = header.isVariadicCall(this, scope);
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = arguments[i].normalize(scope).castImplicit(position, scope, header.getParameterType(isVariadic, i));
		}
		
		if (arguments.length < header.parameters.length) {
			Expression[] newArguments = Arrays.copyOf(arguments, header.parameters.length);
			for (int i = arguments.length; i < header.parameters.length; i++) {
                final FunctionParameter parameter = header.parameters[i];
                if (parameter.defaultValue == null) {
				    if(parameter.variadic) {
				        newArguments[i] = new ArrayExpression(position, Expression.NONE, parameter.type);
                    } else {
                        newArguments[i] = new InvalidExpression(position, parameter.type, CompileExceptionCode.MISSING_PARAMETER, "Parameter missing and no default value specified");
                    }
                } else {
                    newArguments[i] = parameter.defaultValue;
                }
			}
			result = new CallArguments(typeArguments, newArguments);
		}
		return result;
	}
}
