/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Map;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class AssocTypeID implements ITypeID {
	public final ITypeID keyType;
	public final ITypeID valueType;
	
	public AssocTypeID(ITypeID keyType, ITypeID valueType) {
		this.keyType = keyType;
		this.valueType = valueType;
	}
	
	@Override
	public AssocTypeID withGenericArguments(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> arguments) {
		return registry.getAssociative(
				keyType.withGenericArguments(registry, arguments),
				valueType.withGenericArguments(registry, arguments));
	}
	
	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitAssoc(this);
	}
	
	@Override
	public AssocTypeID getUnmodified() {
		return this;
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public boolean isConst() {
		return false;
	}
	
	@Override
	public boolean isObjectType() {
		return true;
	}
	
	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return keyType.hasInferenceBlockingTypeParameters(parameters) || valueType.hasInferenceBlockingTypeParameters(parameters);
	}

	@Override
	public boolean hasDefaultValue() {
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + keyType.hashCode();
		hash = 29 * hash + valueType.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AssocTypeID other = (AssocTypeID) obj;
		return this.keyType == other.keyType && this.valueType == other.valueType;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(valueType.toString());
		result.append('[');
		result.append(keyType.toString());
		result.append(']');
		return result.toString();
	}
}
