/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Map;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;

/**
 *
 * @author Hoofdgebruiker
 */
public class GenericTypeID implements ITypeID {
	public final TypeParameter parameter;

	public GenericTypeID(TypeParameter parameter) {
		this.parameter = parameter;
	}
	
	public boolean matches(LocalMemberCache cache, ITypeID type) {
		return parameter.matches(cache, type);
	}
	
	@Override
	public ITypeID withGenericArguments(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> arguments) {
		return arguments.containsKey(parameter) ? arguments.get(parameter) : this;
	}

	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitGeneric(this);
	}
	
	@Override
	public GenericTypeID getUnmodified() {
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
		return parameter.isObjectType();
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		for (TypeParameter parameter : parameters)
			if (parameter == this.parameter)
				return true;
		
		return false;
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + parameter.hashCode();
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
		final GenericTypeID other = (GenericTypeID) obj;
		return this.parameter == other.parameter;
	}
	
	@Override
	public String toString() {
		return parameter.toString();
	}
}
