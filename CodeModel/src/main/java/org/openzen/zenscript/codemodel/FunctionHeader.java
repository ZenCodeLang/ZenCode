/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;

/**
 *
 * @author Hoofdgebruiker
 */
public class FunctionHeader {
	private static final FunctionParameter[] NO_PARAMETERS = new FunctionParameter[0];
	
	public final TypeParameter[] typeParameters;
	public final ITypeID returnType;
	public final FunctionParameter[] parameters;
	
	public FunctionHeader(ITypeID returnType) {
		this.typeParameters = null;
		this.returnType = returnType;
		this.parameters = NO_PARAMETERS;
	}
	
	public FunctionHeader(ITypeID returnType, FunctionParameter... parameters) {
		this.typeParameters = null;
		this.returnType = returnType;
		this.parameters = parameters;
	}
	
	public FunctionHeader(TypeParameter[] genericParameters, ITypeID returnType, FunctionParameter... parameters) {
		this.typeParameters = genericParameters;
		this.returnType = returnType;
		this.parameters = parameters;
	}
	
	public int getNumberOfTypeParameters() {
		return typeParameters == null ? 0 : typeParameters.length;
	}
	
	public FunctionHeader instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		TypeParameter[] genericParameters = this.typeParameters == null ? null : new TypeParameter[this.typeParameters.length];
		if (genericParameters != null)
			for (int i = 0; i < genericParameters.length; i++)
				genericParameters[i] = this.typeParameters[i].withGenericArguments(registry, mapping);
		
		ITypeID returnType = this.returnType.withGenericArguments(registry, mapping);
		FunctionParameter[] parameters = new FunctionParameter[this.parameters.length];
		for (int i = 0; i < parameters.length; i++)
			parameters[i] = this.parameters[i].withGenericArguments(registry, mapping);
		
		return new FunctionHeader(genericParameters, returnType, parameters);
	}
	
	public ITypeID[] inferTypes(LocalMemberCache cache, CallArguments arguments, List<ITypeID> resultHint) {
		if (arguments.arguments.length != this.parameters.length)
			return null;
		
		Map<TypeParameter, ITypeID> mapping = new HashMap<>();
		if (!resultHint.isEmpty()) {
			Map<TypeParameter, ITypeID> temp = new HashMap<>();
			for (ITypeID hint : resultHint) {
				if (returnType.inferTypeParameters(cache, hint, temp)) {
					mapping = temp;
					break;
				}
			}
		}
		
		// TODO: lambda header inference
		for (int i = 0; i < parameters.length; i++)
			if (!parameters[i].type.inferTypeParameters(cache, arguments.arguments[i].type, mapping))
				return null;
		
		if (mapping.size() > typeParameters.length)
			return null;
		
		ITypeID[] result = new ITypeID[typeParameters.length];
		for (int i = 0; i < typeParameters.length; i++) {
			TypeParameter typeParameter = typeParameters[i];
			if (!mapping.containsKey(typeParameter)) {
				return null;
			} else {
				result[i] = mapping.get(typeParameter);
			}
		}
		
		return result;
	}
	
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		for (int i = 0; i < this.parameters.length; i++)
			if (this.parameters[i].type.hasInferenceBlockingTypeParameters(parameters))
				return true;
		
		return false;
	}
	
	public boolean canCastTo(TypeScope scope, FunctionHeader header) {
		if (parameters.length != header.parameters.length)
			return false;
		
		if (!scope.getTypeMembers(returnType).canCastImplicit(header.returnType))
			return false;
		
		for (int i = 0; i < parameters.length; i++) {
			if (!scope.getTypeMembers(header.parameters[i].type).canCastImplicit(parameters[i].type))
				return false;
		}
		
		return true;
	}
	
	public boolean accepts(TypeScope scope, Expression... arguments) {
		if (parameters.length != arguments.length)
			return false;
		
		for (int i = 0; i < arguments.length; i++) {
			if (!scope.getTypeMembers(arguments[i].type).canCastImplicit(parameters[i].type))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if two function headers are equivalent. Functions headers are
	 * equivalent if their types are the same.
	 * 
	 * @param other
	 * @return 
	 */
	public boolean isEquivalentTo(FunctionHeader other) {
		if (parameters.length != other.parameters.length)
			return false;
		
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].type != other.parameters[i].type)
				return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if two function headers are similar. "similar" means that there
	 * exists a set of parameters for which there is no way to determine which
	 * one to call.
	 * 
	 * Note that this does not mean that there is never confusion about which
	 * method to call. There can be confusion due to implicit conversions. This
	 * can be resolved by performing the conversions explicitly.
	 * 
	 * It is illegal to have two similar methods with the same name.
	 * 
	 * @param other
	 * @return 
	 */
	public boolean isSimilarTo(FunctionHeader other) {
		int common = Math.min(parameters.length, other.parameters.length);
		for (int i = 0; i < common; i++) {
			if (parameters[i].type != other.parameters[i].type)
				return false;
		}
		for (int i = common; i < parameters.length; i++) {
			if (parameters[i].defaultValue == null)
				return false;
		}
		for (int i = common; i < other.parameters.length; i++) {
			if (other.parameters[i].defaultValue == null)
				return false;
		}
		
		return true;
	}
	
	public FunctionHeader withGenericArguments(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> arguments) {
		ITypeID returnType = this.returnType.withGenericArguments(registry, arguments);
		FunctionParameter[] parameters = new FunctionParameter[this.parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			ITypeID modified = this.parameters[i].type.withGenericArguments(registry, arguments);
			parameters[i] = modified == this.parameters[i].type ? this.parameters[i] : new FunctionParameter(modified, this.parameters[i].name);
		}
		return new FunctionHeader(typeParameters, returnType, parameters);
	}
	
	public FunctionHeader withGenericArguments(GlobalTypeRegistry registry, ITypeID[] arguments) {
		Map<TypeParameter, ITypeID> typeArguments = new HashMap<>();
		if (typeParameters != null)
			for (int i = 0; i < typeParameters.length; i++)
				typeArguments.put(typeParameters[i], arguments[i]);
		
		ITypeID returnType = this.returnType.withGenericArguments(registry, typeArguments);
		FunctionParameter[] parameters = new FunctionParameter[this.parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			ITypeID modified = this.parameters[i].type.withGenericArguments(registry, typeArguments);
			parameters[i] = modified == this.parameters[i].type ? this.parameters[i] : new FunctionParameter(modified, this.parameters[i].name);
		}
		return new FunctionHeader(returnType, parameters);
	}
	
	public FunctionHeader forTypeParameterInference() {
		return new FunctionHeader(BasicTypeID.UNDETERMINED, parameters);
	}
	
	public FunctionHeader forLambda(FunctionHeader lambdaHeader) {
		FunctionParameter[] parameters = new FunctionParameter[lambdaHeader.parameters.length];
		for (int i = 0; i < lambdaHeader.parameters.length; i++)
			parameters[i] = new FunctionParameter(this.parameters[i].type, lambdaHeader.parameters[i].name);
		
		return new FunctionHeader(typeParameters, returnType, parameters);
		//return this;
	}
	
	public FunctionParameter getVariadicParameter() {
		if (parameters.length == 0)
			return null;
		if (parameters[parameters.length - 1].variadic)
			return parameters[parameters.length - 1];
		
		return null;
	}
	
	public String explainWhyIncompatible(TypeScope scope, CallArguments arguments) {
		if (this.parameters.length != arguments.arguments.length)
			return parameters.length + " parameters expected but " + arguments.arguments.length + " given.";
		
		if (getNumberOfTypeParameters() != arguments.getNumberOfTypeArguments())
			return typeParameters.length + " type parameters expected but " + arguments.typeArguments.length + " given.";
		
		for (int i = 0; i < parameters.length; i++) {
			if (!scope.getTypeMembers(arguments.arguments[i].getType()).canCastImplicit(parameters[i].type)) {
				return "Parameter " + i + ": cannot cast " + arguments.arguments[i].getType() + " to " + parameters[i].type;
			}
		}
		
		return "Method should be compatible";
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		if (typeParameters.length > 0) {
			result.append("<");
			for (int i = 0; i < typeParameters.length; i++) {
				if (i > 0)
					result.append(", ");
				result.append(typeParameters[i].toString());
			}
			result.append(">");
		}
		result.append("(");
		for (int i = 0; i < parameters.length; i++) {
			if (i > 0)
				result.append(", ");
			result.append(parameters[i].toString());
		}
		result.append(")");
		return result.toString();
	}
}
