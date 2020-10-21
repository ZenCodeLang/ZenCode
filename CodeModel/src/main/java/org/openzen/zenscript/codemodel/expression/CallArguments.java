/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.Arrays;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class CallArguments {
	public static final CallArguments EMPTY = new CallArguments(Expression.NONE);
	
	public final TypeID[] typeArguments;
	public final Expression[] arguments;
	
	public CallArguments(Expression... arguments) {
		this.typeArguments = TypeID.NONE;
		this.arguments = arguments;
	}
	
	public CallArguments(TypeID[] typeArguments, Expression[] arguments) {
		if (typeArguments == null)
			typeArguments = TypeID.NONE;
		if (arguments == null)
			throw new IllegalArgumentException("Arguments cannot be null!");
		
		this.typeArguments = typeArguments;
		this.arguments = arguments;
	}
	
	public CallArguments(StoredType... dummy) {
		this.typeArguments = TypeID.NONE;
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
				if (header.parameters[i].defaultValue == null)
					newArguments[i] = new InvalidExpression(position, header.parameters[i].type, CompileExceptionCode.MISSING_PARAMETER, "Parameter missing and no default value specified");
				else
					newArguments[i] = header.parameters[i].defaultValue;
			}
			result = new CallArguments(typeArguments, newArguments);
		}
		return result;
	}
}
